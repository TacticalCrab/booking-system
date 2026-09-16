package com.example.bookingsystem.booking.event;

import java.time.LocalDateTime;

public record BookingCreatedEvent(
        Long bookingId,
        String customerEmail,
        String serviceName,
        LocalDateTime startTime
) {}
