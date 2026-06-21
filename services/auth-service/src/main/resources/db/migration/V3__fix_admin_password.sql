-- ------------------------------------------------------------------
-- V3: Repair the seeded ADMIN password hash.
--
-- The V1 seed embedded a corrupt BCrypt hash: the cost-10 hash of the
-- string "password" was hand-edited to claim cost 12 ($2a$10$ -> $2a$12$)
-- without re-hashing, so the digest no longer matched the cost prefix and
-- verified against NO password — the documented `Admin@12345` credential
-- could never log in.
--
-- This migration installs a genuine BCrypt cost-12 hash of `Admin@12345`.
-- (V1 is left untouched so its Flyway checksum stays valid on existing DBs.)
-- ------------------------------------------------------------------
UPDATE users
SET password_hash         = '$2a$12$4YASoyV8lNCranyV2xBEmeL5TppUYOltExGTWSgpfapyXvZTtVjhW',
    status                = 'ACTIVE',
    failed_login_attempts = 0,
    locked_until          = NULL
WHERE email = 'admin@smartorder.com';
