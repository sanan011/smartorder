package com.smartorder.auth.adapters.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Inbound DTO for POST /api/v1/auth/logout
 */
@Getter
@NoArgsConstructor
public class LogoutRequest {

    @NotBlank(message = "Refresh token is required")
    private String refreshToken;

    /**
     * If true, revokes all sessions for the user (logout all devices).
     * Defaults to false (single device logout).
     */
    private boolean logoutAllDevices = false;
}