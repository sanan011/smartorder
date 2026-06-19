package com.smartorder.notification.domain.model;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.Instant;
import java.util.Map;

/**
 * MongoDB document storing platform-wide audit events.
 * Consumed from all Kafka topics and persisted here for
 * compliance, debugging, and support tooling.
 */
@Getter
@Builder
@Document(collection = "audit_logs")
public class AuditLog {

    @Id
    private String              id;

    @Indexed
    private String              aggregateType;   // User, Product, Order

    @Indexed
    private String              aggregateId;

    @Indexed
    private String              eventType;

    private Map<String, Object> payload;

    @Indexed
    private Instant             occurredAt;

    private String              correlationId;

    private String              sourceService;
}