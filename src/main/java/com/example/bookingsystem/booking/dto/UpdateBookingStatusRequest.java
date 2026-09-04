package com.example.bookingsystem.booking.dto;

import com.example.bookingsystem.booking.BookingStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateBookingStatusRequest(
        @NotNull
        BookingStatus status
) { }
