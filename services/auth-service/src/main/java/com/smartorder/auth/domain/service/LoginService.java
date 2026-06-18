package com.smartorder.auth.domain.service;

import com.smartorder.auth.domain.model.RefreshToken;
import com.smartorder.auth.domain.model.User;
import com.smartorder.auth.domain.model.UserStatus;
import com.smartorder.auth.ports.inbound.LoginUseCase;
import com.smartorder.auth.ports.outbound.AuthEventPublisherPort;
import com.smartorder.auth.ports.outbound.PasswordEncoderPort;
import com.smartorder.auth.ports.outbound.RefreshTokenRepositoryPort;
import com.smartorder.auth.ports.outbound.TokenProviderPort;
import com.smartorder.auth.ports.outbound.UserRepositoryPort;
import com.smartorder.common.exception.ErrorCode;
import com.smartorder.common.exception.SmartOrderException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Use-case implementation for user authentication.
 *
 * Login flow:
 *  1. Look up user by email — 404 → generic INVALID_CREDENTIALS (no user enumeration)
 *  2. Check account status (deleted, suspended, locked)
 *  3. Verify password
 *  4. On failure: record attempt, potentially lock account
 *  5. On success: reset counter, issue token pair, publish event
 */
@Slf4j
@RequiredArgsConstructor
public class LoginService implements LoginUseCase {

    private static final int  MAX_FAILED_ATTEMPTS    = 5;
    private static final long LOCKOUT_DURATION_MS    = 30L * 60 * 1000; // 30 min

    private final UserRepositoryPort        userRepository;
    private final PasswordEncoderPort       passwordEncoder;
    private final TokenProviderPort         tokenProvider;
    private final RefreshTokenRepositoryPort refreshTokenRepository;
    private final AuthEventPublisherPort    eventPublisher;

    @Override
    public Result execute(Command command) {
        log.debug("Login attempt for email: {}", command.email());

        // ── 1. Look up user ──────────────────────────────────
        User user = userRepository.findByEmail(command.email())
                .orElseThrow(() -> new SmartOrderException(ErrorCode.INVALID_CREDENTIALS));

        // ── 2. Check account status ──────────────────────────
        assertAccountUsable(user);

        // ── 3. Verify password ───────────────────────────────
        if (!passwordEncoder.matches(command.password(), user.getPasswordHash())) {
            handleFailedAttempt(user);
            // Always throw INVALID_CREDENTIALS — never reveal which field failed
            throw new SmartOrderException(ErrorCode.INVALID_CREDENTIALS);
        }

        // ── 4. Reset failure counter ─────────────────────────
        user.recordSuccessfulLogin();
        userRepository.save(user);

        // ── 5. Issue token pair ──────────────────────────────
        String accessToken  = tokenProvider.generateAccessToken(user);
        RefreshToken refresh = new RefreshToken(
                user.getId(),
                tokenProvider.getAccessTokenExpiryMs() * 48  // refresh lives 48× longer
        );
        refreshTokenRepository.save(refresh);

        // ── 6. Publish login event ───────────────────────────
        try {
            eventPublisher.publishUserLoggedIn(user, command.ipAddress());
        } catch (Exception e) {
            log.warn("Failed to publish UserLoggedIn event: {}", e.getMessage());
        }

        log.info("Login successful: userId={}", user.getId());

        return new Result(
                accessToken,
                refresh.getTokenValue(),
                tokenProvider.getAccessTokenExpiryMs(),
                user.getId().toString(),
                user.getEmail(),
                user.fullName(),
                user.getRole().name()
        );
    }

    // ── Helpers ──────────────────────────────────────────────

    private void assertAccountUsable(User user) {
        switch (user.getStatus()) {
            case DELETED    -> throw new SmartOrderException(ErrorCode.INVALID_CREDENTIALS);
            case SUSPENDED  -> throw new SmartOrderException(ErrorCode.ACCOUNT_DISABLED,
                    "Account has been suspended.");
            case LOCKED     -> {
                if (user.isCurrentlyLocked()) {
                    throw new SmartOrderException(ErrorCode.ACCOUNT_DISABLED,
                            "Account is temporarily locked due to too many failed login attempts.");
                }
                // Lock window has passed — allow attempt to proceed
            }
            default -> { /* ACTIVE or PENDING_VERIFICATION — allow through */ }
        }
    }

    private void handleFailedAttempt(User user) {
        user.recordFailedLogin(MAX_FAILED_ATTEMPTS, LOCKOUT_DURATION_MS);
        userRepository.save(user);

        if (user.getStatus() == UserStatus.LOCKED) {
            log.warn("Account locked after failed attempts: userId={}", user.getId());
            try {
                eventPublisher.publishAccountLocked(user);
            } catch (Exception e) {
                log.warn("Failed to publish AccountLocked event: {}", e.getMessage());
            }
        }
    }
}