package com.example.bookingsystem.auth;

import com.example.bookingsystem.auth.dto.LoginRequest;
import com.example.bookingsystem.auth.dto.RegisterRequest;
import com.example.bookingsystem.user.dto.UserResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/api/auth")
class AuthController {

    private final AuthService service;
    private final SecurityContextRepository securityContextRepository;

    public AuthController(
            AuthService authService,
            SecurityContextRepository securityContextRepository
    ) {
        service = authService;
        this.securityContextRepository = securityContextRepository;
    }

    @PostMapping("/register")
    public UserResponse register(
            @Valid @RequestBody RegisterRequest request
    ) {
        return service.register(request);
    }

    @PostMapping("/login")
    public UserResponse login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        LoginResult result = service.login(request);

        SecurityContext context =
                SecurityContextHolder.createEmptyContext();

        context.setAuthentication(result.authentication());
        securityContextRepository.saveContext(
                context,
                httpRequest,
                httpResponse
        );

        return result.user();
    }
}
