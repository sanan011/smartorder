package com.smartorder.auth.ports.outbound;

import com.smartorder.auth.domain.model.User;

/**
 * Outbound port — publishes domain events to the message broker.
 *
 * Events are consumed by:
 *  - notification-service  (sends welcome emails, security alerts)
 *  - audit/logging service (persists to MongoDB)
 *
 * The adapter implementation uses Kafka.
 */
public interface AuthEventPublisherPort {

    /**
     * Published when a new user completes registration.
     * Triggers: welcome email, audit log entry.
     */
    void publishUserRegistered(User user);

    /**
     * Published when a user successfully logs in.
     * Triggers: audit log entry, anomaly detection.
     */
    void publishUserLoggedIn(User user, String ipAddress);

    /**
     * Published when an account is locked due to failed attempts.
     * Triggers: security alert email to the user.
     */
    void publishAccountLocked(User user);

    /**
     * Published when a user's password is changed.
     * Triggers: security notification email, revoke all sessions.
     */
    void publishPasswordChanged(User user);

    /**
     * Published when an account is soft-deleted.
     * Triggers: GDPR data retention pipeline.
     */
    void publishUserDeleted(User user);
}