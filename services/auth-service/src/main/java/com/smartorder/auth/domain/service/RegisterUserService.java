package com.smartorder.auth.domain.service;

import com.smartorder.auth.domain.model.Role;
import com.smartorder.auth.domain.model.User;
import com.smartorder.auth.ports.inbound.RegisterUserUseCase;
import com.smartorder.auth.ports.outbound.AuthEventPublisherPort;
import com.smartorder.auth.ports.outbound.PasswordEncoderPort;
import com.smartorder.auth.ports.outbound.UserRepositoryPort;
import com.smartorder.common.exception.ErrorCode;
import com.smartorder.common.exception.SmartOrderException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

/**
 * Use-case implementation for user registration.
 *
 * Deliberately annotated with NO Spring stereotypes here —
 * this is a pure domain service. The Spring @Service annotation
 * is applied in the adapter layer's configuration to keep
 * the domain framework-free.
 */
@Slf4j
@RequiredArgsConstructor
public class RegisterUserService implements RegisterUserUseCase {

    private final UserRepositoryPort    userRepository;
    private final PasswordEncoderPort   passwordEncoder;
    private final AuthEventPublisherPort eventPublisher;

    private static final int PASSWORD_MIN_LENGTH = 8;

    @Override
    public UUID execute(Command command) {
        log.debug("Registering new user with email: {}", command.email());

        // ── Guard: email uniqueness ──────────────────────────
        if (userRepository.existsByEmail(command.email())) {
            throw new SmartOrderException(
                    ErrorCode.EMAIL_ALREADY_REGISTERED,
                    "email=" + command.email()
            );
        }

        // ── Guard: password strength ─────────────────────────
        validatePassword(command.password());

        // ── Determine role ───────────────────────────────────
        Role role = (command.role() != null) ? command.role() : Role.CUSTOMER;

        // ── Create domain object ─────────────────────────────
        String encodedPassword = passwordEncoder.encode(command.password());
        User user = new User(
                command.email(),
                encodedPassword,
                command.firstName(),
                command.lastName(),
                role
        );

        // ── Persist ──────────────────────────────────────────
        User savedUser = userRepository.save(user);
        log.info("User registered successfully: id={}, email={}",
                savedUser.getId(), savedUser.getEmail());

        // ── Publish domain event ─────────────────────────────
        // Fire-and-forget: registration succeeds even if event publish fails.
        // The Transactional Outbox Pattern handles reliability at infra level.
        try {
            eventPublisher.publishUserRegistered(savedUser);
        } catch (Exception e) {
            log.warn("Failed to publish UserRegistered event for userId={}: {}",
                    savedUser.getId(), e.getMessage());
        }

        return savedUser.getId();
    }

    private void validatePassword(String password) {
        if (password == null || password.length() < PASSWORD_MIN_LENGTH) {
            throw new SmartOrderException(
                    ErrorCode.VALIDATION_FAILED,
                    "Password must be at least " + PASSWORD_MIN_LENGTH + " characters long."
            );
        }
        // Additional rules: at least one digit, one uppercase
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