package com.smartorder.auth.adapters.web.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * Outbound DTO for GET /api/v1/auth/me
 */
@Getter
@Builder
public class UserProfileResponse {
    private final String userId;
    private final String email;
    private final String role;
}