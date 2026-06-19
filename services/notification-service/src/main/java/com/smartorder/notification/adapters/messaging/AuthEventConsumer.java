package com.smartorder.notification.adapters.messaging;

import com.smartorder.notification.adapters.persistence.AuditLogRepository;
import com.smartorder.notification.adapters.persistence.NotificationLogRepository;
import com.smartorder.notification.domain.model.AuditLog;
import com.smartorder.notification.domain.model.NotificationLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;

/**
 * Consumes auth domain events from Kafka and:
 *  1. Persists an audit log entry to MongoDB
 *  2. Sends the appropriate notification (email/etc.)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthEventConsumer {

    private final NotificationLogRepository notificationLogRepo;
    private final AuditLogRepository        auditLogRepo;
    private final EmailNotificationService  emailService;

    @KafkaListener(
            topics   = "smartorder.auth.user-registered",
            groupId  = "notification-service-group"
    )
    public void onUserRegistered(Map<String, Object> event) {
        log.info("Received UserRegistered event: userId={}",
                event.get("userId"));

        persistAudit(event, "User", "USER_REGISTERED");

        String email    = (String) event.get("email");
        String fullName = (String) event.get("fullName");

        if (email != null) {
            emailService.sendWelcomeEmail(email, fullName, event, notificationLogRepo);
        }
    }

    @KafkaListener(
            topics  = "smartorder.auth.account-locked",
            groupId = "notification-service-group"
    )
    public void onAccountLocked(Map<String, Object> event) {
        log.warn("Received AccountLocked event: userId={}", event.get("userId"));

        persistAudit(event, "User", "ACCOUNT_LOCKED");

        String email = (String) event.get("email");
        if (email != null) {
            emailService.sendAccountLockedEmail(email, event, notificationLogRepo);
        }
    }

    @KafkaListener(
            topics  = "smartorder.auth.password-changed",
            groupId = "notification-service-group"
    )
    public void onPasswordChanged(Map<String, Object> event) {
        log.info("Received PasswordChanged event: userId={}", event.get("userId"));
        persistAudit(event, "User", "PASSWORD_CHANGED");

        String email = (String) event.get("email");
        if (email != null) {
            emailService.sendPasswordChangedEmail(email, event, notificationLogRepo);
        }
    }

    @KafkaListener(
            topics  = "smartorder.auth.user-logged-in",
            groupId = "notification-service-group"
    )
    public void onUserLoggedIn(Map<String, Object> event) {
        // Audit only — no notification for login
        persistAudit(event, "User", "USER_LOGGED_IN");
    }

    @KafkaListener(
            topics  = "smartorder.auth.user-deleted",
            groupId = "notification-service-group"
    )
    public void onUserDeleted(Map<String, Object> event) {
        log.info("Received UserDeleted event: userId={}", event.get("userId"));
        persistAudit(event, "User", "USER_DELETED");
    }

    // ── Helper ────────────────────────────────────────────────

    private void persistAudit(Map<String, Object> event,
                              String aggregateType,
                              String eventType) {
        try {
            auditLogRepo.save(AuditLog.builder()
                    .aggregateType(aggregateType)
                    .aggregateId((String) event.get("userId"))
                    .eventType(eventType)
                    .payload(event)
                    .occurredAt(parseInstant(event.get("occurredAt")))
                    .sourceService("auth-service")
                    .build());
        } catch (Exception e) {
            log.error("Failed to persist audit log for {}: {}",
                    eventType, e.getMessage());
        }
    }

    private Instant parseInstant(Object value) {
        if (value == null) return Instant.now();
        try { return Instant.parse(value.toString()); }
        catch (Exception e) { return Instant.now(); }
    }
}