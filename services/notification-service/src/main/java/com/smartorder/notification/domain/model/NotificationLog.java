package com.smartorder.notification.domain.model;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.Instant;

/**
 * MongoDB document storing every notification event.
 * Used for audit trails, retry logic, and support queries.
 */
@Getter
@Builder
@Document(collection = "notification_logs")
public class NotificationLog {

    @Id
    private String  id;

    @Indexed
    private String  userId;

    @Indexed
    private String  eventType;       // e.g. USER_REGISTERED, PRODUCT_APPROVED

    private String  channel;         // EMAIL, SMS, PUSH

    private String  recipient;       // email address or phone

    private String  subject;

    private String  body;

    private String  status;          // SENT, FAILED, SKIPPED

    private String  failureReason;

    @Indexed
    private Instant occurredAt;

    private Instant processedAt;

    private String  correlationId;
}