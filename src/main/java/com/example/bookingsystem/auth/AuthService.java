package com.example.bookingsystem.auth;

import com.example.bookingsystem.auth.dto.*;
import com.example.bookingsystem.auth.exception.AuthenticationFailedException;
import com.example.bookingsystem.auth.refresh.IssuedRefreshToken;
import com.example.bookingsystem.auth.refresh.RefreshTokenService;
import com.example.bookingsystem.auth.refresh.RotatedRefreshToken;
import com.example.bookingsystem.user.dto.CreateUserRequest;
import com.example.bookingsystem.user.*;
import com.example.bookingsystem.user.dto.UserResponse;
import jakarta.transaction.Transactional;
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
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public AuthService(
            UserService userService,
            UserRepository userRepository,
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            RefreshTokenService refreshTokenService
    ) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
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

    public LoginResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(
                            request.email(),
                            request.password()
                    )
            );

            User user = userRepository
                    .findByEmail(authentication.getName())
                    .orElseThrow(AuthenticationFailedException::new);

            String accessToken = jwtService.generateToken(
                    new JwtUserClaims(
                            user.getId(),
                            user.getEmail(),
                            user.getRole()
                    )
            );

            IssuedRefreshToken refreshToken =
                    refreshTokenService.create(user);

            return new LoginResponse(
                    accessToken,
                    refreshToken.token(),
                    UserMapper.toResponse(user)
            );

        } catch (AuthenticationException e) {
            throw new AuthenticationFailedException();
        }
    }

    @Transactional
    public RefreshResponse refresh(RefreshRequest request) {
        RotatedRefreshToken rotated =
                refreshTokenService.rotate(request.refreshToken());

        User user = rotated.user();

        JwtUserClaims claims = new JwtUserClaims(
                user.getId(),
                user.getEmail(),
                user.getRole()
        );

        String accessToken = jwtService.generateToken(claims);

        return new RefreshResponse(
                accessToken,
                rotated.token()
        );
    }

    public void logout(LogoutRequest request) {
        refreshTokenService.revoke(request.refreshToken());
    }

    public void logoutAll(String email) {
        User user = userRepository
                .findByEmail(email)
                .orElseThrow(AuthenticationFailedException::new);

        refreshTokenService.revokeAll(user.getId());
    }
}
