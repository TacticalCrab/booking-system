package com.example.bookingsystem.booking.dto;

import com.example.bookingsystem.booking.BookingStatus;
import com.example.bookingsystem.service.dto.ServiceResponse;
import com.example.bookingsystem.user.dto.UserResponse;

import java.time.LocalDateTime;

public record BookingResponse(
        Long id,
        UserResponse user,
        BookingEmployeeResponse employee,
        ServiceResponse service,
        LocalDateTime startTime,
        LocalDateTime endTime,
        BookingStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) { }
