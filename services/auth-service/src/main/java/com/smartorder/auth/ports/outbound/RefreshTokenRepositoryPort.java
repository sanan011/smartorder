package com.smartorder.auth.ports.outbound;

import com.smartorder.auth.domain.model.RefreshToken;

import java.util.Optional;
import java.util.UUID;

/**
 * Outbound port — persistence contract for RefreshToken.
 * Backed by Redis in the adapter layer (TTL-managed storage).
 */
public interface RefreshTokenRepositoryPort {

    /**
     * Stores the refresh token in Redis with its natural TTL.
     */
    void save(RefreshToken token);

    /**
     * Looks up a refresh token by its raw string value.
     */
    Optional<RefreshToken> findByTokenValue(String tokenValue);

    /**
     * Revokes a single token (marks revoked flag in Redis).
     */
    void revoke(String tokenValue);

    /**
     * Revokes ALL refresh tokens belonging to a user.
     * Called on password change, account suspension, or logout-all-devices.
     */
    void revokeAllByUserId(UUID userId);

    /**
     * Returns the count of active (non-revoked, non-expired) tokens
     * for a user. Used to enforce max concurrent session limits.
     */
    long countActiveTokensByUserId(UUID userId);
}