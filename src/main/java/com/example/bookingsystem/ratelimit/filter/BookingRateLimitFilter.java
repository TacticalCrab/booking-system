package com.example.bookingsystem.ratelimit.filter;


import com.example.bookingsystem.ratelimit.RateLimitProperties;
import com.example.bookingsystem.ratelimit.RateLimitService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class BookingRateLimitFilter extends OncePerRequestFilter {
    private final RateLimitService rateLimitService;
    private final RateLimitProperties properties;

    public BookingRateLimitFilter(
            RateLimitService rateLimitService,
            RateLimitProperties properties
    ) {
        this.rateLimitService = rateLimitService;
        this.properties = properties;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        if (!isBookingCreation(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            filterChain.doFilter(request, response);
            return;
        }

        String userId = authentication.getName();

        boolean allowed = rateLimitService.allowRequest(
                userId,
                properties.booking()
        );

        if (!allowed) {
            response.setStatus(
                    HttpStatus.TOO_MANY_REQUESTS.value()
            );

            response.setHeader("Retry-After", "60");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isBookingCreation(
            HttpServletRequest request
    ) {
        return HttpMethod.POST.matches(request.getMethod())
                && request.getRequestURI()
                .equals("/api/bookings");
    }
}
