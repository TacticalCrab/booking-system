package com.example.bookingsystem.employee;

import com.example.bookingsystem.service.ServiceEntity;
import com.example.bookingsystem.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

public class EmployeeTest {

    @Test
    void shouldReturnTrueWhenEmployeeProvidesService() {
        ServiceEntity service = TestDataFactory.serviceBuilder()
                .build();

        Employee employee = TestDataFactory.employeeBuilder()
                .withServices(
                        new ArrayList<>(List.of(service))
                ).build();

        assertTrue(
                employee.providesService(service)
        );
    }

    @Test
    void shouldReturnFalseWhenEmployeeDoesNotProvideService() {
        ServiceEntity service = TestDataFactory.serviceBuilder()
                .build();

        ServiceEntity providedService = TestDataFactory.serviceBuilder()
                .build();

        Employee employee = TestDataFactory.employeeBuilder()
                .withServices(List.of(providedService))
                .build();

        assertFalse(
                employee.providesService(service)
        );
    }
}
