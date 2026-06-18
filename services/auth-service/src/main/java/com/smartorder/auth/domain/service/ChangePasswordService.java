package com.smartorder.auth.domain.service;

import com.smartorder.auth.domain.model.User;
import com.smartorder.auth.ports.inbound.ChangePasswordUseCase;
import com.smartorder.auth.ports.outbound.AuthEventPublisherPort;
import com.smartorder.auth.ports.outbound.PasswordEncoderPort;
import com.smartorder.auth.ports.outbound.RefreshTokenRepositoryPort;
import com.smartorder.auth.ports.outbound.UserRepositoryPort;
import com.smartorder.common.exception.ErrorCode;
import com.smartorder.common.exception.SmartOrderException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Use-case implementation for authenticated password change.
 *
 * Flow:
 *  1. Load user — verify they exist and are active
 *  2. Verify current password
 *  3. Validate new password strength
 *  4. Encode and persist new password
 *  5. Revoke ALL existing refresh tokens (force re-login everywhere)
 *  6. Publish PasswordChanged event
 */
@Slf4j
@RequiredArgsConstructor
public class ChangePasswordService implements ChangePasswordUseCase {

    private static final int PASSWORD_MIN_LENGTH = 8;

    private final UserRepositoryPort         userRepository;
    private final PasswordEncoderPort        passwordEncoder;
    private final RefreshTokenRepositoryPort refreshTokenRepository;
    private final AuthEventPublisherPort     eventPublisher;

    @Override
    public void execute(Command command) {
        // ── 1. Load user ─────────────────────────────────────
        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new SmartOrderException(
                        ErrorCode.USER_NOT_FOUND, "userId=" + command.userId()
                ));

        if (!user.isActive()) {
            throw new SmartOrderException(ErrorCode.ACCOUNT_DISABLED);
        }

        // ── 2. Verify current password ───────────────────────
        if (!passwordEncoder.matches(command.currentPassword(), user.getPasswordHash())) {
            throw new SmartOrderException(
                    ErrorCode.INVALID_CREDENTIALS,
                    "Current password is incorrect."
            );
        }

        // ── 3. Validate new password ─────────────────────────
        validatePassword(command.newPassword());

        if (passwordEncoder.matches(command.newPassword(), user.getPasswordHash())) {
            throw new SmartOrderException(
                    ErrorCode.VALIDATION_FAILED,
                    "New password must differ from the current password."
            );
        }

        // ── 4. Encode and persist ────────────────────────────
        user.changePassword(passwordEncoder.encode(command.newPassword()));
        userRepository.save(user);
        log.info("Password changed for userId={}", command.userId());

        // ── 5. Revoke all sessions ───────────────────────────
        refreshTokenRepository.revokeAllByUserId(command.userId());

        // ── 6. Publish event ─────────────────────────────────
        try {
            eventPublisher.publishPasswordChanged(user);
        } catch (Exception e) {
            log.warn("Failed to publish PasswordChanged event: {}", e.getMessage());
        }
    }

    private void validatePassword(String password) {
        if (password == null || password.length() < PASSWORD_MIN_LENGTH) {
            throw new SmartOrderException(
                    ErrorCode.VALIDATION_FAILED,
                    "Password must be at least " + PASSWORD_MIN_LENGTH + " characters."
            );
        }
        boolean hasDigit     = password.chars().anyMatch(Character::isDigit);
        boolean hasUppercase = password.chars().anyMatch(Character::isUpperCase);
        if (!hasDigit || !hasUppercase) {
            throw new SmartOrderException(
                    ErrorCode.VALIDATION_FAILED,
                    "Password must contain at least one uppercase letter and one digit."
            );
        }
    }
}