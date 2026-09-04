package com.example.bookingsystem.auth;

import com.example.bookingsystem.user.UserRole;

public record JwtUserClaims(
        Long userId,
        String email,
        UserRole role
) {}
