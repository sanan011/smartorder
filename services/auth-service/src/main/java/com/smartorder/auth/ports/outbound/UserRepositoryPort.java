package com.smartorder.auth.ports.outbound;

import com.smartorder.auth.domain.model.User;

import java.util.Optional;
import java.util.UUID;

/**
 * Outbound port — persistence contract for User aggregate.
 * The domain and use-case layers depend ONLY on this interface.
 * The actual Spring Data JPA implementation lives in the adapters layer.
 */
public interface UserRepositoryPort {

    /**
     * Persists a new user or updates an existing one.
     * Returns the saved user (may include DB-generated fields).
     */
    User save(User user);

    /**
     * Finds a user by their internal UUID.
     */
    Optional<User> findById(UUID id);

    /**
     * Finds a user by email address (case-insensitive lookup
     * is enforced at the adapter level).
     */
    Optional<User> findByEmail(String email);

    /**
     * Returns true if any non-deleted user already owns this email.
     */
    boolean existsByEmail(String email);

    /**
     * Hard-deletes a user record. Should only be called by
     * scheduled GDPR purge jobs — normal deletion uses soft-delete
     * via {@link User#softDelete()}.
     */
    void hardDeleteById(UUID id);
}