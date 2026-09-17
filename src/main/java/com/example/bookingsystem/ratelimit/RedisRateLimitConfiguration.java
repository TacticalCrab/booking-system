package com.example.bookingsystem.ratelimit;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.RedisScript;

@Configuration
public class RedisRateLimitConfiguration {

    @Bean
    public RedisScript<Long> tokenBucketScript() {
        return RedisScript.of(
                new ClassPathResource(
                        "redis/token-bucket.lua"
                ),
                Long.class
        );
    }

}
