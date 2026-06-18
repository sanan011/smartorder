package com.smartorder.auth.adapters.web.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * Outbound DTO for successful registration.
 */
@Getter
@Builder
public class RegisterResponse {
    private final String userId;
    private final String email;
    private final String message;
}