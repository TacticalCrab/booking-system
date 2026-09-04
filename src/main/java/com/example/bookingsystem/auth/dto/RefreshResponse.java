package com.example.bookingsystem.auth.dto;

public record RefreshResponse(
        String accessToken,
        String refreshToken
) {}
