package com.smartorder.auth.adapters.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Declares Kafka topics for the Auth Service.
 * Topics are created automatically on startup if they don't exist.
 * In production, manage topics via Terraform or Kafka AdminClient.
 */
@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic userRegisteredTopic() {
        return TopicBuilder.name("smartorder.auth.user-registered")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic userLoggedInTopic() {
        return TopicBuilder.name("smartorder.auth.user-logged-in")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic accountLockedTopic() {
        return TopicBuilder.name("smartorder.auth.account-locked")
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic passwordChangedTopic() {
        return TopicBuilder.name("smartorder.auth.password-changed")
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic userDeletedTopic() {
        return TopicBuilder.name("smartorder.auth.user-deleted")
                .partitions(1)
                .replicas(1)
                .build();
    }
}