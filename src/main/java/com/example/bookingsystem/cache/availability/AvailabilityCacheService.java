package com.example.bookingsystem.cache.availability;

import com.example.bookingsystem.cache.CacheNames;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
class AvailabilityCacheService {
    public final CacheManager cacheManager;
    public final StringRedisTemplate redisTemplate;

    public AvailabilityCacheService(
            CacheManager cacheManager,
            StringRedisTemplate redisTemplate
    ) {
        this.cacheManager = cacheManager;
        this.redisTemplate = redisTemplate;
    }

    public void evictForEmployee(Long employeeId) {
        String pattern = CacheNames.EMPLOYEE_AVAILABILITY
                + "::"
                + employeeId
                + ":*";

        ScanOptions options = ScanOptions.scanOptions()
                .match(pattern)
                .count(100)
                .build();

        List<String> keys = new ArrayList<>();

        try (Cursor<String> cursor = redisTemplate.scan(options)) {
            cursor.forEachRemaining(keys::add);
        }

        if (!keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    public void evictForEmployeeAndDate(
            Long employeeId,
            List<Long> serviceIds,
            LocalDate date
    ) {
        Cache cache = cacheManager.getCache(CacheNames.EMPLOYEE_AVAILABILITY);

        if (cache == null) {
            return;
        }

        for (Long serviceId: serviceIds) {
            String key = employeeId
                    + ":"
                    + serviceId
                    + ":"
                    + date;

            cache.evict(key);
        }
    }
}
