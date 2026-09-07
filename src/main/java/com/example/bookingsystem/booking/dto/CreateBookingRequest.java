package com.example.bookingsystem.booking.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

public record CreateBookingRequest(
        @NotNull
        @Positive
        Long employeeId,

        @NotNull
        @Positive
        Long serviceId,

        @NotNull
        @Future
        LocalDateTime startTime
) { }
