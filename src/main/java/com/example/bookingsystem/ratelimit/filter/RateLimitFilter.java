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
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitService rateLimitService;
    private final RateLimitProperties rateLimitProperties;

    public RateLimitFilter(
            RateLimitService rateLimitService,
            RateLimitProperties rateLimitProperties
    ) {
        this.rateLimitService = rateLimitService;
        this.rateLimitProperties = rateLimitProperties;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        if (!isLoginRequest(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String ip = request.getRemoteAddr();

        boolean allowed = rateLimitService
                .allowRequest(ip, rateLimitProperties.login());

        if (!allowed) {
            response.setStatus(
                    HttpStatus.TOO_MANY_REQUESTS.value()
            );

            response.setHeader(
                    "Retry-After",
                    "60"
            );

            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isLoginRequest(
            HttpServletRequest request
    ) {
        return HttpMethod.POST.matches(request.getMethod())
                && request.getRequestURI()
                .equals("/api/auth/login");
    }
}
