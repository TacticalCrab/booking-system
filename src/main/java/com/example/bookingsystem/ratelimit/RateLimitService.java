package com.example.bookingsystem.ratelimit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RateLimitService {

    private final StringRedisTemplate redisTemplate;
    private final RedisScript<Long> tokenBucketScript;

    public RateLimitService(
            StringRedisTemplate redisTemplate,
            RedisScript<Long> tokenBucketScript
    ) {
        this.redisTemplate = redisTemplate;
        this.tokenBucketScript = tokenBucketScript;
    }

    public boolean allowRequest(
            String key,
            RateLimitProperties.Limit limit
    ) {
        long ttlSeconds = calculateTtl(limit);

        Long result = redisTemplate.execute(
                tokenBucketScript,
                List.of("rate-limit:" + key),
                String.valueOf(limit.capacity()),
                String.valueOf(limit.refillPerMinute()),
                String.valueOf(ttlSeconds)
        );

        return Long.valueOf(1).equals(result);
    }

    private long calculateTtl(
            RateLimitProperties.Limit limit
    ) {
        double secondsToFull =
                ((double) limit.capacity()
                    / limit.refillPerMinute())
                    * 60;

        return Math.max(
                60,
                (long) Math.ceil(secondsToFull * 2)
        );
    }

}
