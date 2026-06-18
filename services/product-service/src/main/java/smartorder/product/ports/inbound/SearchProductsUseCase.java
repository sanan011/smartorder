package com.smartorder.product.ports.inbound;

import com.smartorder.product.domain.model.Product;

import java.util.List;

/**
 * Inbound port — product search and autocomplete.
 */
public interface SearchProductsUseCase {

    /**
     * Full-text search with filters and pagination.
     */
    Result search(Query query);

    /**
     * Returns autocomplete suggestions for the search bar.
     */
    List<String> autocomplete(String prefix);

    record Query(
            String query,
            String categoryId,
            Double minPrice,
            Double maxPrice,
            String sortBy,      // "price_asc", "price_desc", "rating", "newest"
            int    page,
            int    size
    ) {}

    record Result(
            List<Product> products,
            long          totalHits,
            int           page,
            int           size,
            int           totalPages
    ) {}
}