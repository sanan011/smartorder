package com.smartorder.auth.domain.model;

import java.time.Instant;
import java.util.UUID;

/**
 * Core User aggregate root — pure Java, no framework annotations.
 *
 * This class lives in the domain layer and encapsulates all
 * business rules related to user identity and account lifecycle.
 * The JPA entity (in the adapters layer) maps to/from this object.
 */
public class User {

    private final UUID   id;
    private String       email;
    private String       passwordHash;
    private String       firstName;
    private String       lastName;
    private Role         role;
    private UserStatus   status;
    private int          failedLoginAttempts;
    private Instant      lockedUntil;
    private Instant      createdAt;
    private Instant      updatedAt;
    private String       createdBy;

    // ── Constructor (used when creating a brand-new user) ───

    public User(String email,
                String passwordHash,
                String firstName,
                String lastName,
                Role role) {
        this.id                  = UUID.randomUUID();
        this.email               = email.toLowerCase().trim();
        this.passwordHash        = passwordHash;
        this.firstName           = firstName;
        this.lastName            = lastName;
        this.role                = role;
        // No email-verification flow exists in the BRD-delivered scope, so new
        // accounts are immediately ACTIVE. Leaving them PENDING_VERIFICATION made
        // login work but refresh-token and change-password reject the account
        // (ACCOUNT_DISABLED), breaking the acceptance §9.1 token lifecycle.
        this.status              = UserStatus.ACTIVE;
        this.failedLoginAttempts = 0;
        this.createdAt           = Instant.now();
        this.updatedAt           = Instant.now();
        this.createdBy           = "SYSTEM";
    }

    // ── Reconstitution constructor (used by repository) ─────

    public User(UUID id,
                String email,
                String passwordHash,
                String firstName,
                String lastName,
                Role role,
                UserStatus status,
                int failedLoginAttempts,
                Instant lockedUntil,
                Instant createdAt,
                Instant updatedAt,
                String createdBy) {
        this.id                  = id;
        this.email               = email;
        this.passwordHash        = passwordHash;
        this.firstName           = firstName;
        this.lastName            = lastName;
        this.role                = role;
        this.status              = status;
        this.failedLoginAttempts = failedLoginAttempts;
        this.lockedUntil         = lockedUntil;
        this.createdAt           = createdAt;
        this.updatedAt           = updatedAt;
        this.createdBy           = createdBy;
    }

    // ── Business rules ───────────────────────────────────────

    /**
     * Activates the account after email verification.
     * Throws if account is not in PENDING_VERIFICATION state.
     */
    public void activate() {
        if (this.status != UserStatus.PENDING_VERIFICATION) {
            throw new IllegalStateException(
                    "Cannot activate account in status: " + this.status
            );
        }
        this.status    = UserStatus.ACTIVE;
        this.updatedAt = Instant.now();
    }

    /**
     * Records a failed login attempt.
     * Locks the account after maxAttempts consecutive failures.
     *
     * @param maxAttempts       platform-configured threshold
     * @param lockoutDurationMs how long to lock (milliseconds)
     */
    public void recordFailedLogin(int maxAttempts, long lockoutDurationMs) {
        this.failedLoginAttempts++;
        this.updatedAt = Instant.now();

        if (this.failedLoginAttempts >= maxAttempts) {
            this.status      = UserStatus.LOCKED;
            this.lockedUntil = Instant.now().plusMillis(lockoutDurationMs);
        }
    }

    /**
     * Resets the failed login counter after a successful login.
     */
    public void recordSuccessfulLogin() {
        this.failedLoginAttempts = 0;
        this.lockedUntil         = null;
        this.updatedAt           = Instant.now();

        // Auto-unlock if lock window has expired
        if (this.status == UserStatus.LOCKED) {
            this.status = UserStatus.ACTIVE;
        }
    }

    /**
     * Returns true if the account is currently locked and the
     * lockout window has not yet expired.
     */
    public boolean isCurrentlyLocked() {
        if (this.status != UserStatus.LOCKED) return false;
        if (this.lockedUntil == null)          return false;
        return Instant.now().isBefore(this.lockedUntil);
    }

    /**
     * Soft-deletes the account. Data is retained for audit purposes.
     * Email is anonymised to free the address for re-registration.
     */
    public void softDelete() {
        this.status    = UserStatus.DELETED;
        this.email     = "deleted_" + this.id + "@smartorder.invalid";
        this.updatedAt = Instant.now();
    }

    /**
     * Suspends the account. Only ADMIN can do this — enforced at
     * the use-case layer, not here.
     */
    public void suspend() {
        if (this.status == UserStatus.DELETED) {
            throw new IllegalStateException("Cannot suspend a deleted account.");
        }
        this.status    = UserStatus.SUSPENDED;
        this.updatedAt = Instant.now();
    }

    public void changePassword(String newPasswordHash) {
        this.passwordHash = newPasswordHash;
        this.updatedAt    = Instant.now();
    }

    public String fullName() {
        return this.firstName + " " + this.lastName;
    }

    public boolean isActive() {
        return this.status == UserStatus.ACTIVE;
    }

    // ── Getters (no setters — mutations go through methods) ──

    public UUID      getId()                  { return id; }
    public String    getEmail()               { return email; }
    public String    getPasswordHash()        { return passwordHash; }
    public String    getFirstName()           { return firstName; }
    public String    getLastName()            { return lastName; }
    public Role      getRole()                { return role; }
    public UserStatus getStatus()             { return status; }
    public int       getFailedLoginAttempts() { return failedLoginAttempts; }
    public Instant   getLockedUntil()         { return lockedUntil; }
    public Instant   getCreatedAt()           { return createdAt; }
    public Instant   getUpdatedAt()           { return updatedAt; }
    public String    getCreatedBy()           { return createdBy; }
}