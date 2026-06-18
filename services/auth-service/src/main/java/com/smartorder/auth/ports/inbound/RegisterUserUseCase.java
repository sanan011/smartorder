package com.smartorder.auth.ports.inbound;

import com.smartorder.auth.domain.model.Role;

import java.util.UUID;

/**
 * Inbound port — driving port for user registration.
 * The REST adapter calls this; the use-case service implements it.
 */
public interface RegisterUserUseCase {

    /**
     * Registers a new user account.
     *
     * @param command the registration command
     * @return the newly created user's UUID
     * @throws com.smartorder.common.exception.SmartOrderException
     *         with EMAIL_ALREADY_REGISTERED if email is taken
     */
    UUID execute(Command command);

    record Command(
            String email,
            String password,
            String firstName,
            String lastName,
            Role   role        // defaults to CUSTOMER if null
    ) {}
}