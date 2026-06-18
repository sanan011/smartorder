package com.smartorder.product.ports.outbound;

import com.smartorder.product.domain.model.Product;

/**
 * Outbound port — publishes product domain events to Kafka.
 *
 * Consumers:
 *  - inventory-service  (create initial stock record on ProductCreated)
 *  - notification-service (notify seller on approval/rejection)
 *  - search indexer     (keep Elasticsearch in sync)
 */
public interface ProductEventPublisherPort {

    void publishProductCreated(Product product);

    void publishProductUpdated(Product product);

    void publishProductApproved(Product product);

    void publishProductRejected(Product product, String reason);

    void publishProductDeleted(String productId, String sellerId);
}