package com.example.bookingsystem.auth.refresh;

import java.time.Instant;

public record IssuedRefreshToken(
    String token,
    Instant expiresAt
) { }
