package com.ipl.auction.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Order(1)
@Slf4j
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final int MAX_REQUESTS_PER_MINUTE = 120;
    private final Map<String, RequestBucket> clientBuckets = new ConcurrentHashMap<>();

    private static class RequestBucket {
        final AtomicInteger count = new AtomicInteger(0);
        volatile long resetTime = System.currentTimeMillis() + 60000L;

        boolean allowRequest(int maxRequests) {
            long now = System.currentTimeMillis();
            if (now > resetTime) {
                synchronized (this) {
                    if (now > resetTime) {
                        count.set(0);
                        resetTime = now + 60000L;
                    }
                }
            }
            return count.incrementAndGet() <= maxRequests;
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // Skip rate-limiting for static docs, swagger, and WebSocket handshakes
        if (path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs") ||
            path.startsWith("/ws-auction") || path.startsWith("/actuator")) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = getClientIp(request);
        RequestBucket bucket = clientBuckets.computeIfAbsent(clientIp, k -> new RequestBucket());

        if (!bucket.allowRequest(MAX_REQUESTS_PER_MINUTE)) {
            log.warn("Rate limit exceeded for IP: {} on URI: {}", clientIp, path);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"success\":false,\"message\":\"Too many requests. Please slow down.\",\"data\":null}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty() || "unknown".equalsIgnoreCase(xfHeader)) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0].trim();
    }
}
