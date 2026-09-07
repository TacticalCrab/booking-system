package com.example.bookingsystem.employee.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record ReplaceEmployeeWorkingHoursRequest(
        @NotEmpty
        List<@Valid WorkingHoursRequest> workingDays
) {}
