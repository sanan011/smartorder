package com.smartorder.auth.domain.service;

import com.smartorder.auth.domain.model.RefreshToken;
import com.smartorder.auth.ports.inbound.LogoutUseCase;
import com.smartorder.auth.ports.outbound.RefreshTokenRepositoryPort;
import com.smartorder.common.exception.ErrorCode;
import com.smartorder.common.exception.SmartOrderException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Use-case implementation for logout.
 *
 * Single-device logout: revokes only the provided refresh token.
 * All-devices logout:   revokes every refresh token for the user.
 */
@Slf4j
@RequiredArgsConstructor
public class LogoutService implements LogoutUseCase {

    private final RefreshTokenRepositoryPort refreshTokenRepository;

    @Override
    public void execute(Command command) {
        RefreshToken storedToken = refreshTokenRepository
                .findByTokenValue(command.refreshToken())
                .orElseThrow(() -> new SmartOrderException(
                        ErrorCode.TOKEN_INVALID, "Refresh token not found."
                ));

        if (command.logoutAllDevices()) {
            refreshTokenRepository.revokeAllByUserId(storedToken.getUserId());
            log.info("All sessions revoked for userId={}", storedToken.getUserId());
        } else {
            refreshTokenRepository.revoke(command.refreshToken());
            log.info("Single session revoked for userId={}", storedToken.getUserId());
        }
    }
}