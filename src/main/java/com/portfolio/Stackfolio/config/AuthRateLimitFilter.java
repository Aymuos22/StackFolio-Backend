package com.portfolio.Stackfolio.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Clock;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AuthRateLimitFilter extends OncePerRequestFilter {

    private final Map<String, RequestWindow> requests = new ConcurrentHashMap<>();
    private final int maxRequests;
    private final long windowMillis;
    private final Clock clock;

    public AuthRateLimitFilter(
            @Value("${stackfolio.rate-limit.auth.max-requests:20}") int maxRequests,
            @Value("${stackfolio.rate-limit.auth.window-seconds:60}") long windowSeconds
    ) {
        this.maxRequests = maxRequests;
        this.windowMillis = windowSeconds * 1000;
        this.clock = Clock.systemUTC();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !path.equals("/api/auth/signin")
                && !path.equals("/api/auth/signup")
                && !path.equals("/api/auth/forgot-password")
                && !path.equals("/api/auth/reset-password");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String key = clientKey(request);
        long now = clock.millis();

        RequestWindow window = requests.compute(key, (ignored, existing) -> {
            if (existing == null || now >= existing.resetAtMillis()) {
                return new RequestWindow(1, now + windowMillis);
            }
            return new RequestWindow(
                    existing.count() + 1,
                    existing.resetAtMillis()
            );
        });

        if (window.count() > maxRequests) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write("""
                    {"message":"Too many authentication requests"}
                    """);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String clientKey(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private record RequestWindow(int count, long resetAtMillis) {
    }
}
