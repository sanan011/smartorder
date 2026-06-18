package com.smartorder.auth.ports.outbound;

/**
 * Outbound port — password hashing abstraction.
 *
 * Keeping this as a port (rather than importing BCrypt directly
 * into use cases) means we can swap the hashing algorithm without
 * touching any business logic.
 */
public interface PasswordEncoderPort {

    /**
     * Hashes a raw plaintext password.
     * The returned hash is safe to persist.
     */
    String encode(String rawPassword);

    /**
     * Verifies a raw password against a stored hash.
     *
     * @param rawPassword   the plaintext candidate
     * @param encodedPassword the stored hash
     * @return true if they match
     */
    boolean matches(String rawPassword, String encodedPassword);
}