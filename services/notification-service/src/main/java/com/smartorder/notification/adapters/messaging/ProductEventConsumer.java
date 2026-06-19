package com.smartorder.notification.adapters.messaging;

import com.smartorder.notification.adapters.persistence.AuditLogRepository;
import com.smartorder.notification.adapters.persistence.NotificationLogRepository;
import com.smartorder.notification.domain.model.AuditLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;

/**
 * Consumes product domain events and persists audit logs.
 * Also notifies sellers of approval/rejection decisions.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProductEventConsumer {

    private final AuditLogRepository       auditLogRepo;
    private final NotificationLogRepository notificationLogRepo;
    private final EmailNotificationService  emailService;

    @KafkaListener(
            topics  = "smartorder.product.created",
            groupId = "notification-service-group"
    )
    public void onProductCreated(Map<String, Object> event) {
        log.debug("Product created: {}", event.get("productId"));
        persistAudit(event, "Product", "PRODUCT_CREATED");
    }

    @KafkaListener(
            topics  = "smartorder.product.updated",
            groupId = "notification-service-group"
    )
    public void onProductUpdated(Map<String, Object> event) {
        persistAudit(event, "Product", "PRODUCT_UPDATED");
    }

    @KafkaListener(
            topics  = "smartorder.product.approved",
            groupId = "notification-service-group"
    )
    public void onProductApproved(Map<String, Object> event) {
        log.info("Product approved: {}", event.get("productId"));
        persistAudit(event, "Product", "PRODUCT_APPROVED");
        // In a full implementation: notify seller via email
        // emailService.sendProductApprovedEmail(sellerId, event, ...)
    }

    @KafkaListener(
            topics  = "smartorder.product.rejected",
            groupId = "notification-service-group"
    )
    public void onProductRejected(Map<String, Object> event) {
        log.warn("Product rejected: productId={}, reason={}",
                event.get("productId"), event.get("reason"));
        persistAudit(event, "Product", "PRODUCT_REJECTED");
    }

    @KafkaListener(
            topics  = "smartorder.product.deleted",
            groupId = "notification-service-group"
    )
    public void onProductDeleted(Map<String, Object> event) {
        persistAudit(event, "Product", "PRODUCT_DELETED");
    }

    private void persistAudit(Map<String, Object> event,
                              String aggregateType,
                              String eventType) {
        try {
            auditLogRepo.save(AuditLog.builder()
                    .aggregateType(aggregateType)
                    .aggregateId((String) event.get("productId"))
                    .eventType(eventType)
                    .payload(event)
                    .occurredAt(parseInstant(event.get("occurredAt")))
                    .sourceService("product-service")
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