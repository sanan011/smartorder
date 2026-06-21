package com.smartorder.product.ports.outbound;

import com.smartorder.product.domain.model.Product;

import java.util.List;

/**
 * Outbound port — Elasticsearch full-text search contract.
 * The adapter implementation uses the Elasticsearch Java client.
 */
public interface ProductSearchPort {

    /**
     * Indexes or re-indexes a single product document.
     * Called after every product save/update.
     */
    void indexProduct(Product product);

    /**
     * Removes a product document from the search index.
     * Called on soft-delete.
     */
    void removeProduct(String productId);

    /**
     * Full-text search with optional filters.
     *
     * @param query       free-text search term
     * @param categoryId  optional category filter (null = all)
     * @param minPrice    optional minimum price filter (null = no min)
     * @param maxPrice    optional maximum price filter (null = no max)
     * @param sortBy      field to sort by: "price", "rating", "newest"
     * @param page        zero-based page number
     * @param size        page size
     * @return list of matching product IDs in ranked order
     */
    SearchResult search(String query,
                        String categoryId,
                        Double minPrice,
                        Double maxPrice,
                        String sortBy,
                        int page,
                        int size);

    /**
     * Autocomplete suggestions for the search bar.
     * Returns up to 10 product name suggestions.
     */
    List<String> autocomplete(String prefix);

    record SearchResult(
            List<String> productIds,
            long         totalHits,
            int          page,
            int          size
    ) {}
}