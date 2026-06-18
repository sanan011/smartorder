package com.smartorder.product.ports.outbound;

import com.smartorder.product.domain.model.Product;
import com.smartorder.product.domain.model.ProductStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Outbound port — persistence contract for Product aggregate.
 */
public interface ProductRepositoryPort {

    Product save(Product product);

    Optional<Product> findById(UUID id);

    Optional<Product> findBySlug(String slug);

    Optional<Product> findBySkuAndSellerId(String sku, UUID sellerId);

    /**
     * Returns paginated products for a seller filtered by status.
     */
    List<Product> findBySellerId(UUID sellerId, ProductStatus status,
                                 int page, int size);

    /**
     * Returns paginated products by category.
     */
    List<Product> findByCategoryId(UUID categoryId, int page, int size);

    /**
     * Returns products pending admin review.
     */
    List<Product> findByStatus(ProductStatus status, int page, int size);

    /**
     * Total count for a seller's products by status (for pagination).
     */
    long countBySellerId(UUID sellerId, ProductStatus status);

    boolean existsBySlug(String slug);

    boolean existsBySkuAndSellerId(String sku, UUID sellerId);

    void deleteById(UUID id);
}