package com.smartorder.auth.ports.inbound;

import java.util.UUID;

/**
 * Inbound port — driving port for authenticated password change.
 */
public interface ChangePasswordUseCase {

    void execute(Command command);

    record Command(
            UUID   userId,
            String currentPassword,
            String newPassword
    ) {}
}