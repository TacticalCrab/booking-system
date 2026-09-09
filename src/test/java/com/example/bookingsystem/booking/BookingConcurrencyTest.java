package com.example.bookingsystem.booking;

import com.example.bookingsystem.booking.dto.BookingResponse;
import com.example.bookingsystem.booking.dto.CreateBookingRequest;
import com.example.bookingsystem.booking.exception.BookingConflictException;
import com.example.bookingsystem.employee.Employee;
import com.example.bookingsystem.employee.EmployeeRepository;
import com.example.bookingsystem.service.ServiceEntity;
import com.example.bookingsystem.service.ServiceRepository;
import com.example.bookingsystem.support.PostgresIntegrationTest;
import com.example.bookingsystem.support.TestDataFactory;
import com.example.bookingsystem.user.User;
import com.example.bookingsystem.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static com.example.bookingsystem.support.TestDateTimeFactory.future;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BookingConcurrencyTest
        extends PostgresIntegrationTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    private User customer;
    private ServiceEntity service;
    private Employee employee;

    @BeforeEach
    void setUp() {
        bookingRepository.deleteAll();
        employeeRepository.deleteAll();
        serviceRepository.deleteAll();
        userRepository.deleteAll();

        customer = userRepository.save(
                TestDataFactory.userBuilder()
                        .withId(null)
                        .withEmail("customer@example.com")
                        .build()
        );

        service = serviceRepository.save(
                TestDataFactory.serviceBuilder()
                        .withId(null)
                        .build()
        );

        employee = employeeRepository.save(
                TestDataFactory.employeeBuilder()
                        .withId(null)
                        .withEmail("employee@example.com")
                        .withServices(
                                new ArrayList<>(List.of(service))
                        )
                        .withWorkingHours(
                                TestDataFactory.workingHoursAllWeekWithoutIds()
                        )
                        .build()
        );
    }

    @Test
    void shouldAllowOnlyOneBookingWhenTwoRequestsTargetSameSlot()
            throws Exception {

        LocalDateTime startTime =
                future(DayOfWeek.MONDAY, 10, 0);

        CreateBookingRequest request = new CreateBookingRequest(
                employee.getId(),
                service.getId(),
                startTime
        );

        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        Callable<Object> bookingAttempt = () -> {
            ready.countDown();

            start.await();

            try {
                return bookingService.create(
                        customer.getEmail(),
                        request
                );
            } catch (Exception exception) {
                return exception;
            }
        };

        try (
                ExecutorService executor =
                        Executors.newFixedThreadPool(2)
        ) {
            Future<Object> first =
                    executor.submit(bookingAttempt);

            Future<Object> second =
                    executor.submit(bookingAttempt);

            assertTrue(
                    ready.await(5, TimeUnit.SECONDS)
            );

            start.countDown();

            Object firstResult =
                    first.get(10, TimeUnit.SECONDS);

            Object secondResult =
                    second.get(10, TimeUnit.SECONDS);

            List<Object> results = List.of(
                    firstResult,
                    secondResult
            );

            long successes = results.stream()
                    .filter(BookingResponse.class::isInstance)
                    .count();

            long conflicts = results.stream()
                    .filter(BookingConflictException.class::isInstance)
                    .count();

            assertEquals(1, successes);
            assertEquals(1, conflicts);
        }

        long confirmedBookings =
                bookingRepository.countByEmployeeIdAndStatus(
                        employee.getId(),
                        BookingStatus.CONFIRMED
                );

        assertEquals(1, confirmedBookings);
    }
}