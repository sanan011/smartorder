package com.smartorder.product.adapters.search;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import com.smartorder.product.domain.model.Product;
import com.smartorder.product.domain.model.ProductStatus;
import com.smartorder.product.ports.outbound.ProductRepositoryPort;
import com.smartorder.product.ports.outbound.ProductSearchPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.StringReader;
import java.util.List;

/**
 * Ensures the Elasticsearch product index exists with an EXPLICIT mapping.
 *
 * Relying on dynamic mapping (what happened before) typed {@code status} and
 * {@code categoryId} as analyzed text — so the {@code term} filters in
 * {@link ElasticsearchProductSearchAdapter} never matched and every search
 * returned zero hits — and typed {@code nameSuggest} as a plain object, so the
 * completion suggester used by autocomplete failed with "all shards failed".
 *
 * On first startup (index absent) it creates the index and reindexes all ACTIVE
 * products from the system of record (PostgreSQL), so search works immediately.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ElasticsearchIndexInitializer {

    private final ElasticsearchClient    esClient;
    private final ProductRepositoryPort  productRepository;
    private final ProductSearchPort      productSearch;

    private static final String INDEX = "smartorder-products";

    private static final String MAPPING = """
        {
          "mappings": {
            "properties": {
              "productId":       { "type": "keyword" },
              "name":            { "type": "text"    },
              "description":     { "type": "text"    },
              "brand":           { "type": "text"    },
              "categoryId":      { "type": "keyword" },
              "sellerId":        { "type": "keyword" },
              "price":           { "type": "double"  },
              "currency":        { "type": "keyword" },
              "status":          { "type": "keyword" },
              "averageRating":   { "type": "double"  },
              "reviewCount":     { "type": "integer" },
              "tags":            { "type": "text"    },
              "slug":            { "type": "keyword" },
              "primaryImageUrl": { "type": "keyword" },
              "createdAt":       { "type": "long"    },
              "nameSuggest":     { "type": "completion" }
            }
          }
        }
        """;

    @EventListener(ApplicationReadyEvent.class)
    public void initialize() {
        try {
            boolean exists = esClient.indices().exists(e -> e.index(INDEX)).value();
            if (exists) {
                log.info("Elasticsearch index '{}' already exists — leaving as is", INDEX);
                return;
            }
            esClient.indices().create(CreateIndexRequest.of(c -> c
                    .index(INDEX)
                    .withJson(new StringReader(MAPPING))));
            log.info("Created Elasticsearch index '{}' with explicit mapping", INDEX);
            reindexActiveProducts();
        } catch (Exception e) {
            log.error("Failed to initialize Elasticsearch index '{}': {}", INDEX, e.getMessage());
        }
    }

    private void reindexActiveProducts() {
        int page = 0;
        final int size = 100;
        int total = 0;
        while (true) {
            List<Product> batch = productRepository.findByStatus(ProductStatus.ACTIVE, page, size);
            if (batch.isEmpty()) break;
            for (Product p : batch) {
                try {
                    productSearch.indexProduct(p);
                    total++;
                } catch (Exception ex) {
                    log.warn("Reindex failed for product {}: {}", p.getId(), ex.getMessage());
                }
            }
            if (batch.size() < size) break;
            page++;
        }
        log.info("Reindexed {} ACTIVE product(s) into '{}'", total, INDEX);
    }
}
