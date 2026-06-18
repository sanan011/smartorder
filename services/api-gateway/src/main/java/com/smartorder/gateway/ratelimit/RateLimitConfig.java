package com.smartorder.gateway.ratelimit;

import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Mono;

/**
 * Redis-backed token-bucket rate limiting configuration.
 *
 * Two limiters are defined:
 *  - defaultRateLimiter  : 20 req/s burst 40  — general API traffic
 *  - authRateLimiter     : 5  req/s burst 10  — login / register endpoints
 *
 * Key resolution strategy: per remote IP address.
 * In production behind a load balancer, read X-Forwarded-For instead.
 */
@Configuration
@RequiredArgsConstructor
public class RateLimitConfig {

    private final RateLimitProperties properties;

    // ── Key Resolvers ────────────────────────────────────────

    /**
     * Primary key resolver — used by default on all routes.
     * Resolves to the client's remote IP address.
     */
    @Bean
    @Primary
    public KeyResolver ipKeyResolver() {
        return exchange -> {
            String ip = exchange.getRequest()
                    .getHeaders()
                    .getFirst("X-Forwarded-For");

            if (ip == null || ip.isBlank()) {
                ip = exchange.getRequest()
                        .getRemoteAddress() != null
                        ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                        : "unknown";
            }

            // Take only the first IP if X-Forwarded-For has a chain
            String clientIp = ip.split(",")[0].trim();
            return Mono.just(clientIp);
        };
    }

    /**
     * User-aware key resolver for authenticated routes.
     * Falls back to IP if X-Auth-User-Id header is absent.
     */
    @Bean("userKeyResolver")
    public KeyResolver userKeyResolver() {
        return exchange -> {
            String userId = exchange.getRequest()
                    .getHeaders()
                    .getFirst("X-Auth-User-Id");

            if (userId != null && !userId.isBlank()) {
                return Mono.just("user:" + userId);
            }

            // Fallback to IP
            String ip = exchange.getRequest().getRemoteAddress() != null
                    ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                    : "unknown";
            return Mono.just("ip:" + ip);
        };
    }

    // ── Rate Limiters ────────────────────────────────────────

    /**
     * General API rate limiter.
     * replenishRate = sustained req/s; burstCapacity = max spike.
     */
    @Bean
    @Primary
    public RedisRateLimiter defaultRateLimiter() {
        return new RedisRateLimiter(
                properties.getReplenishRate(),
                properties.getBurstCapacity(),
                1  // requested tokens per request
        );
    }

    /**
     * Stricter limiter for auth endpoints to prevent brute-force.
     */
    @Bean("authRateLimiter")
    public RedisRateLimiter authRateLimiter() {
        return new RedisRateLimiter(
                properties.getAuthReplenishRate(),
                properties.getAuthBurstCapacity(),
                1
        );
    }
}