package com.example.bookingsystem.booking.dto;

import com.example.bookingsystem.booking.BookingStatus;

import java.time.LocalDateTime;

public record CreateBookingRequest(
        Long userId,
        Long employeeId,
        Long serviceId,
        LocalDateTime startTime,
        LocalDateTime endTime,
        BookingStatus status
) { }
