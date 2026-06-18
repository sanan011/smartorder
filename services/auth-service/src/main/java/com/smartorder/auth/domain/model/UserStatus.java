package com.smartorder.auth.domain.model;

/**
 * Lifecycle states of a User account.
 *
 * PENDING_VERIFICATION — registered but email not yet confirmed
 * ACTIVE               — fully operational account
 * LOCKED               — temporarily locked after failed login attempts
 * SUSPENDED            — manually suspended by ADMIN
 * DELETED              — soft-deleted, data retained for audit
 */
public enum UserStatus {
    PENDING_VERIFICATION,
    ACTIVE,
    LOCKED,
    SUSPENDED,
    DELETED
}