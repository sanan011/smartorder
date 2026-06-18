package com.smartorder.auth.adapters.persistence;

import com.smartorder.auth.domain.model.RefreshToken;
import com.smartorder.auth.ports.outbound.RefreshTokenRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Redis adapter for refresh token storage.
 *
 * Key strategy:
 *  - Individual token : "rt:{tokenValue}"          → serialised RefreshToken JSON
 *  - User token index : "rt:user:{userId}"         → Set of tokenValues
 *
 * TTL is set to the token's natural expiry on every write.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RefreshTokenRedisAdapter implements RefreshTokenRepositoryPort {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String TOKEN_PREFIX    = "rt:";
    private static final String USER_IDX_PREFIX = "rt:user:";

    @Override
    public void save(RefreshToken token) {
        String tokenKey   = TOKEN_PREFIX    + token.getTokenValue();
        String userIdxKey = USER_IDX_PREFIX + token.getUserId();

        Duration ttl = Duration.between(Instant.now(), token.getExpiresAt());
        if (ttl.isNegative() || ttl.isZero()) {
            log.warn("Attempted to save already-expired refresh token for userId={}",
                    token.getUserId());
            return;
        }

        // Store serialised token with TTL
        redisTemplate.opsForValue().set(tokenKey, token, ttl);

        // Maintain per-user index (add tokenValue to the set)
        redisTemplate.opsForSet().add(userIdxKey, token.getTokenValue());
        // Extend user index TTL to at least the token's TTL
        redisTemplate.expire(userIdxKey, ttl);

        log.debug("Saved refresh token for userId={}, ttl={}s",
                token.getUserId(), ttl.getSeconds());
    }

    @Override
    public Optional<RefreshToken> findByTokenValue(String tokenValue) {
        String tokenKey = TOKEN_PREFIX + tokenValue;
        Object stored   = redisTemplate.opsForValue().get(tokenKey);

        if (stored instanceof RefreshToken refreshToken) {
            return Optional.of(refreshToken);
        }
        return Optional.empty();
    }

    @Override
    public void revoke(String tokenValue) {
        String tokenKey = TOKEN_PREFIX + tokenValue;
        Object stored   = redisTemplate.opsForValue().get(tokenKey);

        if (stored instanceof RefreshToken token) {
            token.revoke();
            // Persist the revoked state — keep key alive briefly so
            // concurrent requests get a "revoked" response, not "not found"
            redisTemplate.opsForValue().set(tokenKey, token, Duration.ofMinutes(5));

            // Remove from user index
            String userIdxKey = USER_IDX_PREFIX + token.getUserId();
            redisTemplate.opsForSet().remove(userIdxKey, tokenValue);
        }
    }

    @Override
    public void revokeAllByUserId(UUID userId) {
        String userIdxKey = USER_IDX_PREFIX + userId;
        Set<Object> tokenValues = redisTemplate.opsForSet().members(userIdxKey);

        if (tokenValues == null || tokenValues.isEmpty()) return;

        tokenValues.forEach(tv -> {
            if (tv instanceof String tokenValue) {
                revoke(tokenValue);
            }
        });

        redisTemplate.delete(userIdxKey);
        log.info("Revoked all refresh tokens for userId={}", userId);
    }

    @Override
    public long countActiveTokensByUserId(UUID userId) {
        String userIdxKey = USER_IDX_PREFIX + userId;
        Long size = redisTemplate.opsForSet().size(userIdxKey);
        return size != null ? size : 0L;
    }
}