package com.smartorder.auth.ports.outbound;

import com.smartorder.auth.domain.model.User;

import java.util.UUID;

/**
 * Outbound port — JWT generation and validation abstraction.
 *
 * The domain use cases call this port to issue or validate tokens
 * without knowing anything about JJWT or any other JWT library.
 */
public interface TokenProviderPort {

    /**
     * Generates a short-lived JWT access token for the given user.
     * Claims included: sub (userId), username (email), role.
     *
     * @param user the authenticated user
     * @return signed JWT string
     */
    String generateAccessToken(User user);

    /**
     * Validates a JWT access token and returns the subject (userId)
     * if valid.
     *
     * @param token raw JWT string
     * @return the userId encoded in the token's subject claim
     * @throws com.smartorder.common.exception.SmartOrderException
     *         with TOKEN_EXPIRED or TOKEN_INVALID if validation fails
     */
    UUID validateAccessToken(String token);

    /**
     * Extracts the userId from a token WITHOUT validating the signature.
     * Used only in refresh flows where we need the userId before
     * full validation (e.g., to load the stored refresh token).
     *
     * IMPORTANT: never trust the claims returned here for authorization.
     */
    UUID extractUserIdUnchecked(String token);

    /**
     * Returns the configured access token TTL in milliseconds.
     */
    long getAccessTokenExpiryMs();
}