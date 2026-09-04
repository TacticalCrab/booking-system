package com.example.bookingsystem.auth;

import com.example.bookingsystem.auth.dto.LoginRequest;
import com.example.bookingsystem.auth.dto.RegisterRequest;
import com.example.bookingsystem.auth.exception.AuthenticationFailedException;
import com.example.bookingsystem.user.dto.CreateUserRequest;
import com.example.bookingsystem.user.*;
import com.example.bookingsystem.user.dto.UserResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;


@Service
class AuthService {

    private final UserService userService;
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;

    public AuthService(
            UserService userService,
            UserRepository userRepository,
            AuthenticationManager authenticationManager
    ) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
    }

    public UserResponse register(RegisterRequest request) {
        CreateUserRequest createUserRequest = new CreateUserRequest(
                request.email(),
                request.password(),
                request.name(),
                UserRole.CUSTOMER
        );

        return userService.create(createUserRequest);
    }

    public LoginResult login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(
                            request.email(),
                            request.password()
                    )
            );

            User user = userRepository.findByEmail(authentication.getName())
                    .orElseThrow(AuthenticationFailedException::new);

            return new LoginResult(
                    UserMapper.toResponse(user),
                    authentication
            );

        } catch (AuthenticationException e) {
            throw new AuthenticationFailedException();
        }
    }
}
