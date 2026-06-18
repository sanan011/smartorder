package com.smartorder.auth.domain.service;

import com.smartorder.auth.domain.model.RefreshToken;
import com.smartorder.auth.domain.model.User;
import com.smartorder.auth.ports.inbound.RefreshTokenUseCase;
import com.smartorder.auth.ports.outbound.RefreshTokenRepositoryPort;
import com.smartorder.auth.ports.outbound.TokenProviderPort;
import com.smartorder.auth.ports.outbound.UserRepositoryPort;
import com.smartorder.common.exception.ErrorCode;
import com.smartorder.common.exception.SmartOrderException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

/**
 * Use-case implementation for access token renewal.
 *
 * Refresh flow (rotation strategy):
 *  1. Look up the stored refresh token
 *  2. Validate it (not revoked, not expired)
 *  3. Load the owning user — verify they are still active
 *  4. Revoke the old refresh token (one-time use)
 *  5. Issue new access token + new refresh token
 */
@Slf4j
@RequiredArgsConstructor
public class RefreshTokenService implements RefreshTokenUseCase {

    private final RefreshTokenRepositoryPort refreshTokenRepository;
    private final UserRepositoryPort         userRepository;
    private final TokenProviderPort          tokenProvider;

    @Override
    public Result execute(Command command) {
        log.debug("Token refresh requested");

        // ── 1. Look up stored token ──────────────────────────
        RefreshToken storedToken = refreshTokenRepository
                .findByTokenValue(command.refreshToken())
                .orElseThrow(() -> new SmartOrderException(
                        ErrorCode.TOKEN_INVALID, "Refresh token not found."
                ));

        // ── 2. Validate ──────────────────────────────────────
        if (!storedToken.isValid()) {
            // Possible token reuse attack — revoke all sessions
            refreshTokenRepository.revokeAllByUserId(storedToken.getUserId());
            log.warn("Invalid refresh token detected for userId={}. All sessions revoked.",
                    storedToken.getUserId());
            throw new SmartOrderException(
                    ErrorCode.TOKEN_INVALID,
                    "Refresh token is expired or revoked."
            );
        }

        // ── 3. Load user ─────────────────────────────────────
        UUID userId = storedToken.getUserId();
        User user   = userRepository.findById(userId)
                .orElseThrow(() -> new SmartOrderException(
                        ErrorCode.USER_NOT_FOUND, "userId=" + userId
                ));

        if (!user.isActive()) {
            throw new SmartOrderException(ErrorCode.ACCOUNT_DISABLED);
        }

        // ── 4. Revoke old token (rotation) ───────────────────
        refreshTokenRepository.revoke(storedToken.getTokenValue());

        // ── 5. Issue new token pair ──────────────────────────
        String newAccessToken      = tokenProvider.generateAccessToken(user);
        RefreshToken newRefreshToken = new RefreshToken(
                user.getId(),
                tokenProvider.getAccessTokenExpiryMs() * 48
        );
        refreshTokenRepository.save(newRefreshToken);

        log.debug("Token refreshed successfully for userId={}", userId);

        return new Result(
                newAccessToken,
                newRefreshToken.getTokenValue(),
                tokenProvider.getAccessTokenExpiryMs()
        );
    }
}