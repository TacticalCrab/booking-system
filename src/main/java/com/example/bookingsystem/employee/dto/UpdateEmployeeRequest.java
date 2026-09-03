package com.example.bookingsystem.employee.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record UpdateEmployeeRequest(
        @NotBlank
        String name,

        @NotBlank
        @Email
        String email,

        @NotNull
        List<Long> servicesIds
) {}
