package com.smartorder.notification.adapters.persistence;

import com.smartorder.notification.domain.model.AuditLog;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;
import java.util.List;

public interface AuditLogRepository
        extends MongoRepository<AuditLog, String> {

    List<AuditLog> findByAggregateIdOrderByOccurredAtDesc(String aggregateId);

    List<AuditLog> findByEventTypeAndOccurredAtAfter(
            String eventType, Instant after);

    List<AuditLog> findByAggregateTypeAndAggregateId(
            String aggregateType, String aggregateId);
}