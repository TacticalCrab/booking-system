package com.example.bookingsystem.auth.dto;

import com.example.bookingsystem.user.dto.UserResponse;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        UserResponse user
) {}
