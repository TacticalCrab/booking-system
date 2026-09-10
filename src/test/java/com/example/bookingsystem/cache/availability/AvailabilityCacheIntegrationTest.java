package com.example.bookingsystem.cache.availability;

import com.example.bookingsystem.cache.CacheNames;
import com.example.bookingsystem.employee.Employee;
import com.example.bookingsystem.service.ServiceEntity;
import com.example.bookingsystem.support.IntegrationTest;
import com.example.bookingsystem.support.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;


@Execution(ExecutionMode.SAME_THREAD)
@ResourceLock("employeeAvailabilityCache")
class AvailabilityCacheIntegrationTest extends IntegrationTest {

    private static final Long EMPLOYEE_ID = 101L;
    private static final Long OTHER_EMPLOYEE_ID = 202L;
    private static final Long SERVICE_ID = 11L;
    private static final Long OTHER_SERVICE_ID = 22L;
    private static final LocalDate DATE = LocalDate.of(2026, 9, 14);
    private static final LocalDate OTHER_DATE = DATE.plusDays(1);

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private AvailabilityEventPublisher availabilityEventPublisher;

    private Cache availabilityCache;

    @BeforeEach
    void setUp() {
        availabilityCache = cacheManager.getCache(
                CacheNames.EMPLOYEE_AVAILABILITY
        );
        assertNotNull(availabilityCache);
        evictTestKeys();
    }

    @Test
    void evictsCacheWhenEventIsPublishedInCommittedTransaction() {
        put(EMPLOYEE_ID, SERVICE_ID, DATE);

        transactionTemplate.executeWithoutResult(status ->
                availabilityEventPublisher.availabilityChanged(EMPLOYEE_ID)
        );

        assertMissing(EMPLOYEE_ID, SERVICE_ID, DATE);
    }

    @Test
    void keepsCacheWhenEventIsPublishedInRolledBackTransaction() {
        put(EMPLOYEE_ID, SERVICE_ID, DATE);

        transactionTemplate.executeWithoutResult(status -> {
            availabilityEventPublisher.availabilityChanged(EMPLOYEE_ID);
            status.setRollbackOnly();
        });

        assertPresent(EMPLOYEE_ID, SERVICE_ID, DATE);
    }

    @Test
    void employeeAndDateInvalidationRemovesOnlyMatchingKeys() {
        Employee employee = employeeWithServices(SERVICE_ID, OTHER_SERVICE_ID);
        put(EMPLOYEE_ID, SERVICE_ID, DATE);
        put(EMPLOYEE_ID, OTHER_SERVICE_ID, DATE);
        put(EMPLOYEE_ID, SERVICE_ID, OTHER_DATE);
        put(OTHER_EMPLOYEE_ID, SERVICE_ID, DATE);

        transactionTemplate.executeWithoutResult(status ->
                availabilityEventPublisher.availabilityChanged(employee, DATE)
        );

        assertMissing(EMPLOYEE_ID, SERVICE_ID, DATE);
        assertMissing(EMPLOYEE_ID, OTHER_SERVICE_ID, DATE);
        assertPresent(EMPLOYEE_ID, SERVICE_ID, OTHER_DATE);
        assertPresent(OTHER_EMPLOYEE_ID, SERVICE_ID, DATE);
    }

    @Test
    void wholeEmployeeInvalidationRemovesAllEmployeeKeysButKeepsOtherEmployees() {
        put(EMPLOYEE_ID, SERVICE_ID, DATE);
        put(EMPLOYEE_ID, OTHER_SERVICE_ID, OTHER_DATE);
        put(OTHER_EMPLOYEE_ID, SERVICE_ID, DATE);

        transactionTemplate.executeWithoutResult(status ->
                availabilityEventPublisher.availabilityChanged(EMPLOYEE_ID)
        );

        assertMissing(EMPLOYEE_ID, SERVICE_ID, DATE);
        assertMissing(EMPLOYEE_ID, OTHER_SERVICE_ID, OTHER_DATE);
        assertPresent(OTHER_EMPLOYEE_ID, SERVICE_ID, DATE);
    }

    private Employee employeeWithServices(Long... serviceIds) {
        List<ServiceEntity> services = Stream.of(serviceIds)
                .map(serviceId -> TestDataFactory.serviceBuilder()
                        .withId(serviceId)
                        .build())
                .toList();
        Employee employee = new Employee(
                "Employee",
                "employee@example.com",
                services
        );
        employee.setId(EMPLOYEE_ID);
        return employee;
    }

    private void put(Long employeeId, Long serviceId, LocalDate date) {
        availabilityCache.put(key(employeeId, serviceId, date), "cached-value");
    }

    private void evictTestKeys() {
        availabilityCache.evict(key(EMPLOYEE_ID, SERVICE_ID, DATE));
        availabilityCache.evict(key(EMPLOYEE_ID, OTHER_SERVICE_ID, DATE));
        availabilityCache.evict(key(EMPLOYEE_ID, SERVICE_ID, OTHER_DATE));
        availabilityCache.evict(key(OTHER_EMPLOYEE_ID, SERVICE_ID, DATE));
    }

    private void assertPresent(Long employeeId, Long serviceId, LocalDate date) {
        assertNotNull(availabilityCache.get(key(employeeId, serviceId, date)));
    }

    private void assertMissing(Long employeeId, Long serviceId, LocalDate date) {
        assertNull(availabilityCache.get(key(employeeId, serviceId, date)));
    }

    private String key(Long employeeId, Long serviceId, LocalDate date) {
        return employeeId + ":" + serviceId + ":" + date;
    }
}
