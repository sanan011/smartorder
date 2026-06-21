package com.smartorder.product.adapters.search;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.DeleteRequest;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.CompletionSuggestOption;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.smartorder.product.domain.model.Product;
import com.smartorder.product.ports.outbound.ProductSearchPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Elasticsearch adapter implementing {@link ProductSearchPort}.
 *
 * Index name: smartorder-products
 *
 * Document structure:
 * {
 *   "productId":    "uuid",
 *   "name":         "iPhone 15 Pro",
 *   "description":  "...",
 *   "brand":        "Apple",
 *   "categoryId":   "uuid",
 *   "sellerId":     "uuid",
 *   "price":        999.99,
 *   "currency":     "USD",
 *   "status":       "ACTIVE",
 *   "averageRating": 4.8,
 *   "reviewCount":  124,
 *   "tags":         ["smartphone", "5g"],
 *   "primaryImageUrl": "https://...",
 *   "nameSuggest":  { "input": ["iPhone", "15", "Pro"] }
 * }
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ElasticsearchProductSearchAdapter implements ProductSearchPort {

    private final ElasticsearchClient esClient;

    private static final String INDEX = "smartorder-products";

    @Override
    public void indexProduct(Product product) {
        Map<String, Object> doc = buildDocument(product);
        try {
            esClient.index(IndexRequest.of(req -> req
                    .index(INDEX)
                    .id(product.getId().toString())
                    .document(doc)
            ));
            log.debug("Indexed product: id={}", product.getId());
        } catch (IOException e) {
            log.error("Failed to index product id={}: {}", product.getId(), e.getMessage());
            throw new RuntimeException("Elasticsearch indexing failed", e);
        }
    }

    @Override
    public void removeProduct(String productId) {
        try {
            esClient.delete(DeleteRequest.of(req -> req
                    .index(INDEX)
                    .id(productId)
            ));
            log.debug("Removed product from index: id={}", productId);
        } catch (IOException e) {
            log.error("Failed to remove product id={}: {}", productId, e.getMessage());
        }
    }

    @Override
    public SearchResult search(String query,
                               String categoryId,
                               Double minPrice,
                               Double maxPrice,
                               String sortBy,
                               int page,
                               int size) {
        try {
            SearchRequest request = buildSearchRequest(
                    query, categoryId, minPrice, maxPrice, sortBy, page, size
            );

            SearchResponse<Map> response = esClient.search(request, Map.class);

            List<String> productIds = response.hits().hits()
                    .stream()
                    .map(Hit::id)
                    .collect(Collectors.toList());

            long totalHits = response.hits().total() != null
                    ? response.hits().total().value() : 0L;

            return new SearchResult(productIds, totalHits, page, size);

        } catch (Exception e) {
            // Catch ElasticsearchException (unchecked) too — a missing index or
            // mapping must not 500 the homepage product listing; degrade to empty.
            log.error("Elasticsearch search failed: {}", e.getMessage());
            return new SearchResult(List.of(), 0L, page, size);
        }
    }

    @Override
    public List<String> autocomplete(String prefix) {
        try {
            SearchResponse<Map> response = esClient.search(s -> s
                            .index(INDEX)
                            .suggest(sg -> sg
                                    .suggesters("name-suggest", fs -> fs
                                            .prefix(prefix)
                                            .completion(cs -> cs
                                                    .field("nameSuggest")
                                                    .size(10)
                                                    .skipDuplicates(true)
                                            )
                                    )
                            ),
                    Map.class
            );

            List<String> suggestions = new ArrayList<>();
            response.suggest().getOrDefault("name-suggest", List.of())
                    .forEach(suggestion ->
                            suggestion.completion().options().forEach(option ->
                                    suggestions.add(option.text())
                            )
                    );

            return suggestions;

        } catch (IOException e) {
            log.error("Autocomplete failed for prefix='{}': {}", prefix, e.getMessage());
            return List.of();
        }
    }

    // ── Helpers ───────────────────────────────────────────────

    private Map<String, Object> buildDocument(Product product) {
        Map<String, Object> doc = new HashMap<>();
        doc.put("productId",    product.getId().toString());
        doc.put("name",         product.getName());
        doc.put("description",  product.getDescription());
        doc.put("brand",        product.getBrand());
        doc.put("categoryId",   product.getCategoryId().toString());
        doc.put("sellerId",     product.getSellerId().toString());
        doc.put("price",        product.getPrice().getAmount());
        doc.put("currency",     product.getPrice().getCurrencyCode());
        doc.put("status",       product.getStatus().name());
        doc.put("averageRating", product.getAverageRating());
        doc.put("reviewCount",  product.getReviewCount());
        doc.put("tags",         product.getTags());
        doc.put("slug",         product.getSlug());
        // Recency sort field (FR-CAT-07 "newest"). Stored as epoch millis so the
        // sort is a plain numeric sort, independent of date-format mapping.
        doc.put("createdAt",    product.getCreatedAt() != null
                ? product.getCreatedAt().toEpochMilli() : 0L);

        // Primary image for search results display
        if (product.getPrimaryImage() != null) {
            doc.put("primaryImageUrl", product.getPrimaryImage().getUrl());
        }

        // Completion suggester input
        List<String> suggestInputs = new ArrayList<>();
        suggestInputs.add(product.getName());
        if (product.getBrand() != null) suggestInputs.add(product.getBrand());
        suggestInputs.addAll(product.getTags());
        doc.put("nameSuggest", Map.of("input", suggestInputs));

        return doc;
    }

    private SearchRequest buildSearchRequest(String query,
                                             String categoryId,
                                             Double minPrice,
                                             Double maxPrice,
                                             String sortBy,
                                             int page,
                                             int size) {
        return SearchRequest.of(s -> {
            s.index(INDEX)
                    .from(page * size)
                    .size(size);

            // ── Query ────────────────────────────────────────
            s.query(q -> q
                    .bool(b -> {
                        // Full-text on name, description, brand, tags
                        if (query != null && !query.isBlank()) {
                            b.must(m -> m.multiMatch(mm -> mm
                                    .query(query)
                                    .fields("name^3", "brand^2", "tags^1", "description")
                            ));
                        } else {
                            b.must(m -> m.matchAll(ma -> ma));
                        }

                        // Only show ACTIVE products to customers
                        b.filter(f -> f.term(t -> t
                                .field("status").value("ACTIVE")
                        ));

                        // Category filter
                        if (categoryId != null && !categoryId.isBlank()) {
                            b.filter(f -> f.term(t -> t
                                    .field("categoryId").value(categoryId)
                            ));
                        }

                        // Price range filter
                        if (minPrice != null || maxPrice != null) {
                            b.filter(f -> f.range(r -> {
                                r.field("price");
                                if (minPrice != null) r.gte(co.elastic.clients.json.JsonData.of(minPrice));
                                if (maxPrice != null) r.lte(co.elastic.clients.json.JsonData.of(maxPrice));
                                return r;
                            }));
                        }

                        return b;
                    })
            );

            // ── Sort ─────────────────────────────────────────
            if (sortBy != null) {
                switch (sortBy) {
                    case "price_asc"  -> s.sort(so -> so.field(f -> f.field("price").order(SortOrder.Asc)));
                    case "price_desc" -> s.sort(so -> so.field(f -> f.field("price").order(SortOrder.Desc)));
                    case "rating"     -> s.sort(so -> so.field(f -> f.field("averageRating").order(SortOrder.Desc)));
                    case "newest"     -> s.sort(so -> so.field(f -> f.field("createdAt").order(SortOrder.Desc).unmappedType(co.elastic.clients.elasticsearch._types.mapping.FieldType.Long)));
                    // default: relevance score
                }
            }

            return s;
        });
    }
}