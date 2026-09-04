package com.example.bookingsystem.booking.dto;

import com.example.bookingsystem.booking.BookingStatus;
import jakarta.validation.constraints.FutureOrPresent;
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
        @FutureOrPresent
        LocalDateTime startTime
) { }
