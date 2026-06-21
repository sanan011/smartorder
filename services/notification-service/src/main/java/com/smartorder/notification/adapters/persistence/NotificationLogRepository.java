package com.smartorder.notification.adapters.persistence;

import com.smartorder.notification.domain.model.NotificationLog;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface NotificationLogRepository
        extends MongoRepository<NotificationLog, String> {

    List<NotificationLog> findByUserIdOrderByOccurredAtDesc(String userId);

    List<NotificationLog> findByEventTypeAndStatus(String eventType, String status);

    long countByStatus(String status);
}