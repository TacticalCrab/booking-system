package com.example.bookingsystem.cache.availability;

import com.example.bookingsystem.employee.Employee;
import com.example.bookingsystem.service.ServiceEntity;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class AvailabilityEventPublisher {
    private final ApplicationEventPublisher eventPublisher;

    public AvailabilityEventPublisher(
            ApplicationEventPublisher eventPublisher
    ) {
        this.eventPublisher = eventPublisher;
    }

    public void availabilityChanged(
            Long employeeId
    ) {
        eventPublisher.publishEvent(
                new EmployeeAvailabilityChangedEvent(
                        employeeId
                )
        );
    }

    public void availabilityChanged(
            Employee employee,
            LocalDate date
    ) {
        eventPublisher.publishEvent(
                new EmployeeDateAvailabilityChangedEvent(
                        employee.getId(),
                        employee.getServices()
                                .stream()
                                .map(ServiceEntity::getId)
                                .toList(),
                        date
                )
        );
    }
}
