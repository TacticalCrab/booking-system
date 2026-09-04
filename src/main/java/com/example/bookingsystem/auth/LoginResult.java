package com.example.bookingsystem.auth;

import com.example.bookingsystem.user.dto.UserResponse;
import org.springframework.security.core.Authentication;

record LoginResult(
        UserResponse user,
        Authentication authentication
) {}
