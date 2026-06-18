package com.smartorder.auth.ports.inbound;

/**
 * Inbound port — driving port for logout.
 * Revokes the provided refresh token (single device logout).
 * Optionally revokes ALL tokens for the user (logout all devices).
 */
public interface LogoutUseCase {

    void execute(Command command);

    record Command(
            String  refreshToken,
            boolean logoutAllDevices  // if true, revoke all sessions
    ) {}
}