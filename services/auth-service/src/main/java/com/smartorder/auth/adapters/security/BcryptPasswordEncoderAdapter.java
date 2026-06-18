package com.smartorder.auth.adapters.security;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.smartorder.auth.ports.outbound.PasswordEncoderPort;
import org.springframework.stereotype.Component;

/**
 * BCrypt adapter implementing {@link PasswordEncoderPort}.
 * Uses cost factor 12 — balances security and performance.
 * At cost 12, hashing takes ~300ms on modern hardware,
 * making brute-force attacks computationally expensive.
 */
@Component
public class BcryptPasswordEncoderAdapter implements PasswordEncoderPort {

    private static final int BCRYPT_COST = 12;

    @Override
    public String encode(String rawPassword) {
        return BCrypt.withDefaults()
                .hashToString(BCRYPT_COST, rawPassword.toCharArray());
    }

    @Override
    public boolean matches(String rawPassword, String encodedPassword) {
        BCrypt.Result result = BCrypt.verifyer()
                .verify(rawPassword.toCharArray(), encodedPassword);
        return result.verified;
    }
}