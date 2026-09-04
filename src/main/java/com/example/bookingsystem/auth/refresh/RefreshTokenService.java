package com.example.bookingsystem.auth.refresh;

import com.example.bookingsystem.auth.exception.AuthenticationFailedException;
import com.example.bookingsystem.user.User;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;

@Service
public class RefreshTokenService {

    private static final Duration REFRESH_TOKEN_LIFETIME = Duration.ofDays(7);

    private final RefreshTokenRepository repository;
    private final SecureRandom secureRandom = new SecureRandom();

    public RefreshTokenService(
            RefreshTokenRepository refreshTokenRepository
    ) {
        repository = refreshTokenRepository;
    }

    @Transactional
    public IssuedRefreshToken create(User user) {
        return issue(user);
    }

    @Transactional
    public RotatedRefreshToken rotate(String rawToken) {
        RefreshToken oldToken = getValidToken(rawToken);

        oldToken.revoke();

        IssuedRefreshToken newToken =
                issue(oldToken.getUser());

        return new RotatedRefreshToken(
                oldToken.getUser(),
                newToken.token(),
                newToken.expiresAt()
        );
    }

    @Transactional
    public void revoke(String rawToken) {
        String tokenHash = hash(rawToken);

        RefreshToken refreshToken = repository
                .findByTokenHash(tokenHash)
                .orElseThrow(AuthenticationFailedException::new);

        if (!refreshToken.isRevoked()) {
            refreshToken.revoke();
        }
    }

    @Transactional
    public void revokeAll(Long userId) {
        List<RefreshToken> refreshTokens =
                repository.findByUserId(userId);

        refreshTokens.stream()
                .filter(token -> !token.isRevoked())
                .forEach(RefreshToken::revoke);
    }

    private IssuedRefreshToken issue(User user) {
        String rawToken = generateToken();
        String tokenHash = hash(rawToken);

        Instant now = Instant.now();
        Instant expiresAt = now.plus(REFRESH_TOKEN_LIFETIME);

        RefreshToken refreshToken = new RefreshToken(
                user,
                tokenHash,
                expiresAt,
                now
        );

        repository.save(refreshToken);

        return new IssuedRefreshToken(
                rawToken,
                expiresAt
        );
    }

    private RefreshToken getValidToken(String rawToken) {
        String tokenHash = hash(rawToken);

        return repository
                .findByTokenHash(tokenHash)
                .filter(RefreshToken::isValid)
                .orElseThrow(AuthenticationFailedException::new);

    }

    private String generateToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    private String hash(String token) {
        try {
            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    token.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "SHA-256 is not available",
                    exception
            );
        }
    }
}
