package com.smartorder.auth.adapters.messaging;

import com.smartorder.auth.domain.model.User;
import com.smartorder.auth.ports.outbound.AuthEventPublisherPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Kafka adapter implementing {@link AuthEventPublisherPort}.
 *
 * Topic naming convention: smartorder.auth.{event-name}
 * Each event is a self-contained map (schema-less for flexibility;
 * in production consider Avro + Schema Registry).
 *
 * All publishes are fire-and-forget with async callback logging.
 * Reliability is guaranteed by the Transactional Outbox Pattern
 * at the service layer (not implemented here for brevity —
 * the outbox table in V2 migration handles this).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthEventPublisherAdapter implements AuthEventPublisherPort {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String TOPIC_USER_REGISTERED  = "smartorder.auth.user-registered";
    private static final String TOPIC_USER_LOGGED_IN   = "smartorder.auth.user-logged-in";
    private static final String TOPIC_ACCOUNT_LOCKED   = "smartorder.auth.account-locked";
    private static final String TOPIC_PASSWORD_CHANGED = "smartorder.auth.password-changed";
    private static final String TOPIC_USER_DELETED     = "smartorder.auth.user-deleted";

    @Override
    public void publishUserRegistered(User user) {
        Map<String, Object> event = Map.of(
                "eventType",  "USER_REGISTERED",
                "userId",     user.getId().toString(),
                "email",      user.getEmail(),
                "fullName",   user.fullName(),
                "role",       user.getRole().name(),
                "occurredAt", Instant.now().toString()
        );
        send(TOPIC_USER_REGISTERED, user.getId().toString(), event);
    }

    @Override
    public void publishUserLoggedIn(User user, String ipAddress) {
        Map<String, Object> event = Map.of(
                "eventType",  "USER_LOGGED_IN",
                "userId",     user.getId().toString(),
                "email",      user.getEmail(),
                "ipAddress",  ipAddress != null ? ipAddress : "unknown",
                "occurredAt", Instant.now().toString()
        );
        send(TOPIC_USER_LOGGED_IN, user.getId().toString(), event);
    }

    @Override
    public void publishAccountLocked(User user) {
        Map<String, Object> event = Map.of(
                "eventType",  "ACCOUNT_LOCKED",
                "userId",     user.getId().toString(),
                "email",      user.getEmail(),
                "lockedUntil", user.getLockedUntil() != null
                        ? user.getLockedUntil().toString() : "unknown",
                "occurredAt", Instant.now().toString()
        );
        send(TOPIC_ACCOUNT_LOCKED, user.getId().toString(), event);
    }

    @Override
    public void publishPasswordChanged(User user) {
        Map<String, Object> event = Map.of(
                "eventType",  "PASSWORD_CHANGED",
                "userId",     user.getId().toString(),
                "email",      user.getEmail(),
                "occurredAt", Instant.now().toString()
        );
        send(TOPIC_PASSWORD_CHANGED, user.getId().toString(), event);
    }

    @Override
    public void publishUserDeleted(User user) {
        Map<String, Object> event = Map.of(
                "eventType",  "USER_DELETED",
                "userId",     user.getId().toString(),
                "occurredAt", Instant.now().toString()
        );
        send(TOPIC_USER_DELETED, user.getId().toString(), event);
    }

    // ── Internal ─────────────────────────────────────────────

    private void send(String topic, String key, Object payload) {
        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(topic, key, payload);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish event to topic={}, key={}: {}",
                        topic, key, ex.getMessage());
            } else {
                log.debug("Event published to topic={}, partition={}, offset={}",
                        topic,
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });
    }
}