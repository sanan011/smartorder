package com.smartorder.product.domain.service;

import com.smartorder.product.domain.model.Product;
import com.smartorder.product.ports.inbound.SearchProductsUseCase;
import com.smartorder.product.ports.outbound.ProductRepositoryPort;
import com.smartorder.product.ports.outbound.ProductSearchPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class SearchProductsService implements SearchProductsUseCase {

    private final ProductSearchPort     searchPort;
    private final ProductRepositoryPort productRepository;

    @Override
    public Result search(Query query) {
        log.debug("Searching products: query='{}', category={}, page={}",
                query.query(), query.categoryId(), query.page());

        // ── Elasticsearch search ─────────────────────────────
        ProductSearchPort.SearchResult searchResult = searchPort.search(
                query.query(),
                query.categoryId(),
                query.minPrice(),
                query.maxPrice(),
                query.sortBy(),
                query.page(),
                query.size()
        );

        // ── Hydrate from PostgreSQL ──────────────────────────
        // ES gives us ranked IDs; we fetch full objects from DB
        List<Product> products = new ArrayList<>();
        for (String productId : searchResult.productIds()) {
            try {
                productRepository.findById(UUID.fromString(productId))
                        .ifPresent(products::add);
            } catch (Exception e) {
                log.warn("Failed to hydrate product id={}: {}", productId, e.getMessage());
            }
        }

        int totalPages = (int) Math.ceil(
                (double) searchResult.totalHits() / query.size()
        );

        return new Result(
                products,
                searchResult.totalHits(),
                searchResult.page(),
                searchResult.size(),
                totalPages
        );
    }

    @Override
    public List<String> autocomplete(String prefix) {
        if (prefix == null || prefix.trim().length() < 2) {
            return List.of();
        }
        return searchPort.autocomplete(prefix.trim());
    }
}