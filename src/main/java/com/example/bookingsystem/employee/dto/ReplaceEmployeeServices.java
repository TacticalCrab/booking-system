package com.example.bookingsystem.employee.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record ReplaceEmployeeServices(
        @NotEmpty
        List<Long> serviceIds
) {}
