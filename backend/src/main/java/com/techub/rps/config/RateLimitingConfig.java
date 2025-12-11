package com.techub.rps.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
@Slf4j
public class RateLimitingConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new RateLimitingInterceptor())
                .addPathPatterns("/api/**");
    }

    /**
     * Interceptor that implements rate limiting per IP address (30 requests per minute per IP).
     */
    static class RateLimitingInterceptor implements HandlerInterceptor {

        private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
        private final ObjectMapper objectMapper = new ObjectMapper();

        private static final int CAPACITY = 30;
        private static final Duration REFILL_DURATION = Duration.ofMinutes(1);

        @Override
        public boolean preHandle(@NonNull HttpServletRequest request,
                                 @NonNull HttpServletResponse response,
                                 @NonNull Object handler) throws Exception {

            String clientIp = getClientIP(request);
            Bucket bucket = resolveBucket(clientIp);

            if (bucket.tryConsume(1)) {
                long remainingTokens = bucket.getAvailableTokens();
                response.addHeader("X-RateLimit-Limit", String.valueOf(CAPACITY));
                response.addHeader("X-RateLimit-Remaining", String.valueOf(remainingTokens));
                log.debug("Request allowed for IP: ${} (remaining: {})", clientIp, remainingTokens);
                return true;
            } else {
                log.warn("Rate limit exceeded for IP: {}", clientIp);
                RateLimitError error = new RateLimitError(
                        429,
                        "RATE_LIMIT_EXCEEDED",
                        "Too many requests. Please try again later.",
                        "60 seconds"
                );

                response.setStatus(429);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.setCharacterEncoding("UTF-8");
                objectMapper.writeValue(response.getWriter(), error);

                return false;
            }
        }

        /**
         * DTO for rate limit error response.
         */
        private record RateLimitError(
                int status,
                String errorCode,
                String message,
                String retryAfter
        ) {}

        private Bucket resolveBucket(String clientIp) {
            return buckets.computeIfAbsent(clientIp, k -> createNewBucket());
        }

        private Bucket createNewBucket() {
            Bandwidth limit = Bandwidth.builder()
                    .capacity(CAPACITY)
                    .refillIntervally(CAPACITY, REFILL_DURATION)
                    .build();

            return Bucket.builder()
                    .addLimit(limit)
                    .build();
        }

        private String getClientIP(HttpServletRequest request) {
            // Check for IP behind proxy
            String xForwardedFor = request.getHeader("X-Forwarded-For");
            if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                return xForwardedFor.split(",")[0].trim();
            }

            String xRealIP = request.getHeader("X-Real-IP");
            if (xRealIP != null && !xRealIP.isEmpty()) {
                return xRealIP;
            }

            return request.getRemoteAddr();
        }
    }
}
