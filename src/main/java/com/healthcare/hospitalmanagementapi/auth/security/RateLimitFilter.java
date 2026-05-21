package com.healthcare.hospitalmanagementapi.auth.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.hospitalmanagementapi.common.exception.dto.ErrorResponse;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Order(1)
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;   // reuse existing bean like other handlers
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    private Bucket createNewBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(100)
                        .refillIntervally(100, Duration.ofMinutes(1))
                        .build())
                .build();
    }

    // Stricter bucket for auth endpoints (prevent brute force on login/OTP)
    private Bucket createAuthBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(5)
                        .refillIntervally(5, Duration.ofMinutes(1))
                        .build())
                .build();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String clientIp = getClientIp(request);
        String path = request.getRequestURI();

        // Pick bucket based on path
        Bucket bucket = path.startsWith("/api/v1/auth/")
                ? buckets.computeIfAbsent(clientIp + ":auth", k -> createAuthBucket())
                : buckets.computeIfAbsent(clientIp, k -> createNewBucket());

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            ErrorResponse error = ErrorResponse.builder()
                    .code("TOO_MANY_REQUESTS")
                    .message("Rate limit exceeded. Try after sometime.")
                    .timestamp(LocalDateTime.now())
                    .errors(null)
                    .build();

            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            objectMapper.writeValue(response.getOutputStream(), error);
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/actuator");
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        return (forwarded != null) ? forwarded.split(",")[0] : request.getRemoteAddr();
    }
}