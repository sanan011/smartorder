package com.smartorder.product.ports.inbound;

import com.smartorder.product.domain.model.Product;

import java.util.List;
import java.util.UUID;

/**
 * Inbound port — product read operations.
 * Covers single-product lookup and paginated list queries.
 */
public interface GetProductUseCase {

    /**
     * Fetch a single product by ID.
     * Throws PRODUCT_NOT_FOUND if absent or deleted.
     */
    Product findById(UUID productId);

    /**
     * Fetch a single product by URL slug (for SSR product pages).
     */
    Product findBySlug(String slug);

    /**
     * Paginated list of active products in a category.
     */
    List<Product> findByCategory(UUID categoryId, int page, int size);

    /**
     * Paginated list of a seller's own products.
     */
    List<Product> findBySeller(UUID sellerId, String status, int page, int size);
}