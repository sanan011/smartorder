package com.smartorder.auth.domain.model;

import java.time.Instant;
import java.util.UUID;

/**
 * Refresh token domain object.
 * Stored in Redis with TTL matching the expiry field.
 * Rotated on every use (refresh token rotation strategy).
 */
public class RefreshToken {

    private final String  tokenValue;   // the raw token string (stored as Redis key)
    private final UUID    userId;
    private final Instant issuedAt;
    private final Instant expiresAt;
    private       boolean revoked;

    public RefreshToken(UUID userId, long expiryMs) {
        this.tokenValue = UUID.randomUUID().toString();
        this.userId     = userId;
        this.issuedAt   = Instant.now();
        this.expiresAt  = Instant.now().plusMillis(expiryMs);
        this.revoked    = false;
    }

    // Reconstitution constructor
    public RefreshToken(String tokenValue,
                        UUID userId,
                        Instant issuedAt,
                        Instant expiresAt,
                        boolean revoked) {
        this.tokenValue = tokenValue;
        this.userId     = userId;
        this.issuedAt   = issuedAt;
        this.expiresAt  = expiresAt;
        this.revoked    = revoked;
    }

    public boolean isExpired() {
        return Instant.now().isAfter(this.expiresAt);
    }

    public boolean isValid() {
        return !revoked && !isExpired();
    }

    public void revoke() {
        this.revoked = true;
    }

    public String  getTokenValue() { return tokenValue; }
    public UUID    getUserId()     { return userId; }
    public Instant getIssuedAt()   { return issuedAt; }
    public Instant getExpiresAt()  { return expiresAt; }
    public boolean isRevoked()     { return revoked; }
}