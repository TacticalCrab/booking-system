package com.example.bookingsystem.cache.availability;

import java.time.LocalDate;
import java.util.List;

public record EmployeeDateAvailabilityChangedEvent(
        Long employeeId,
        List<Long> serviceIds,
        LocalDate date
) {}
