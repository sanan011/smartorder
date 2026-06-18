package com.smartorder.auth.adapters.web.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * Outbound DTO for login and token refresh responses.
 */
@Getter
@Builder
public class AuthResponse {
    private final String accessToken;
    private final String refreshToken;
    private final long   accessTokenExpiresInMs;
    private final String tokenType = "Bearer";

    // User info (only in login response, null on refresh)
    private final String userId;
    private final String email;
    private final String fullName;
    private final String role;
}