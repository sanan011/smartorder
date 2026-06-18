package com.smartorder.auth.ports.inbound;

/**
 * Inbound port — driving port for user authentication.
 */
public interface LoginUseCase {

    /**
     * Authenticates a user and issues a token pair.
     *
     * @param command login credentials + request metadata
     * @return token pair (access + refresh) plus user info
     * @throws com.smartorder.common.exception.SmartOrderException
     *         with INVALID_CREDENTIALS, ACCOUNT_DISABLED, or TOKEN_INVALID
     */
    Result execute(Command command);

    record Command(
            String email,
            String password,
            String ipAddress    // for audit logging
    ) {}

    record Result(
            String accessToken,
            String refreshToken,
            long   accessTokenExpiresInMs,
            String userId,
            String email,
            String fullName,
            String role
    ) {}
}