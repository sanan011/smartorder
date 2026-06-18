package com.smartorder.auth.ports.inbound;

/**
 * Inbound port — driving port for access token renewal.
 * Implements refresh token rotation: old token is revoked,
 * a new token pair is issued on every call.
 */
public interface RefreshTokenUseCase {

    /**
     * Validates the provided refresh token and issues a new token pair.
     *
     * @param command contains the raw refresh token string
     * @return new access + refresh token pair
     * @throws com.smartorder.common.exception.SmartOrderException
     *         with TOKEN_EXPIRED or TOKEN_INVALID
     */
    Result execute(Command command);

    record Command(String refreshToken) {}

    record Result(
            String accessToken,
            String refreshToken,
            long   accessTokenExpiresInMs
    ) {}
}