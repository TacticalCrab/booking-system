package com.example.bookingsystem.auth.refresh;

import com.example.bookingsystem.user.User;

import java.time.Instant;

public record RotatedRefreshToken(
    User user,
    String token,
    Instant expiresAt
) {}
