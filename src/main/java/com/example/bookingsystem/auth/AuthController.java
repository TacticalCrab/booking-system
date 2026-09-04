package com.example.bookingsystem.auth;

import com.example.bookingsystem.auth.dto.LoginRequest;
import com.example.bookingsystem.auth.dto.LoginResponse;
import com.example.bookingsystem.auth.dto.RegisterRequest;
import com.example.bookingsystem.user.dto.UserResponse;
import jakarta.validation.Valid;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/api/auth")
class AuthController {

    private final AuthService service;

    public AuthController(
            AuthService authService
    ) {
        service = authService;
    }

    @PostMapping("/register")
    public UserResponse register(
            @Valid @RequestBody RegisterRequest request
    ) {
        return service.register(request);
    }

    @PostMapping("/login")
    public LoginResponse login(
            @Valid @RequestBody LoginRequest request
    ) {
        return service.login(request);
    }
}
