package com.example.bookingsystem.employee.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record EmployeeAvailabilityResponse(
        Long employeeId,
        Long serviceId,
        LocalDate date,
        List<LocalTime> slots
) {}
