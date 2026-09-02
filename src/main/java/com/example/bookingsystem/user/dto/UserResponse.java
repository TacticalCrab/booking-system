package com.example.bookingsystem.user.dto;

import com.example.bookingsystem.user.UserRole;

public record UserResponse(
        Long id,
        String email,
        String name,
        UserRole role
) {}
