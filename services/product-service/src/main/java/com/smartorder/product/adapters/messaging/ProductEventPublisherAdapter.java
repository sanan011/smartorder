package com.smartorder.product.adapters.messaging;

import com.smartorder.product.domain.model.Product;
import com.smartorder.product.ports.outbound.ProductEventPublisherPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductEventPublisherAdapter implements ProductEventPublisherPort {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String TOPIC_CREATED  = "smartorder.product.created";
    private static final String TOPIC_UPDATED  = "smartorder.product.updated";
    private static final String TOPIC_APPROVED = "smartorder.product.approved";
    private static final String TOPIC_REJECTED = "smartorder.product.rejected";
    private static final String TOPIC_DELETED  = "smartorder.product.deleted";

    @Override
    public void publishProductCreated(Product product) {
        send(TOPIC_CREATED, product.getId().toString(), Map.of(
                "eventType",   "PRODUCT_CREATED",
                "productId",   product.getId().toString(),
                "sellerId",    product.getSellerId().toString(),
                "name",        product.getName(),
                "categoryId",  product.getCategoryId().toString(),
                "price",       product.getPrice().getAmount(),
                "currency",    product.getPrice().getCurrencyCode(),
                "occurredAt",  Instant.now().toString()
        ));
    }

    @Override
    public void publishProductUpdated(Product product) {
        send(TOPIC_UPDATED, product.getId().toString(), Map.of(
                "eventType",  "PRODUCT_UPDATED",
                "productId",  product.getId().toString(),
                "sellerId",   product.getSellerId().toString(),
                "occurredAt", Instant.now().toString()
        ));
    }

    @Override
    public void publishProductApproved(Product product) {
        send(TOPIC_APPROVED, product.getId().toString(), Map.of(
                "eventType",  "PRODUCT_APPROVED",
                "productId",  product.getId().toString(),
                "sellerId",   product.getSellerId().toString(),
                "name",       product.getName(),
                "occurredAt", Instant.now().toString()
        ));
    }

    @Override
    public void publishProductRejected(Product product, String reason) {
        send(TOPIC_REJECTED, product.getId().toString(), Map.of(
                "eventType",  "PRODUCT_REJECTED",
                "productId",  product.getId().toString(),
                "sellerId",   product.getSellerId().toString(),
                "reason",     reason,
                "occurredAt", Instant.now().toString()
        ));
    }

    @Override
    public void publishProductDeleted(String productId, String sellerId) {
        send(TOPIC_DELETED, productId, Map.of(
                "eventType",  "PRODUCT_DELETED",
                "productId",  productId,
                "sellerId",   sellerId,
                "occurredAt", Instant.now().toString()
        ));
    }

    private void send(String topic, String key, Object payload) {
        kafkaTemplate.send(topic, key, payload).whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish to topic={}: {}", topic, ex.getMessage());
            } else {
                log.debug("Event published to topic={}, offset={}",
                        topic, result.getRecordMetadata().offset());
            }
        });
    }
}