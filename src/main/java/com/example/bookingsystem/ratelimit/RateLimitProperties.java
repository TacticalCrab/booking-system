package com.example.bookingsystem.ratelimit;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rate-limit")
public record RateLimitProperties(
        Limit login,
        Limit booking
) {

    public record Limit(
            int capacity,
            double refillPerMinute
    ) {}
}
