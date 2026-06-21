package com.smartorder.product.domain.service;

import com.smartorder.common.exception.ErrorCode;
import com.smartorder.common.exception.SmartOrderException;
import com.smartorder.product.domain.model.Money;
import com.smartorder.product.domain.model.Product;
import com.smartorder.product.ports.inbound.CreateProductUseCase;
import com.smartorder.product.ports.outbound.CategoryRepositoryPort;
import com.smartorder.product.ports.outbound.ProductEventPublisherPort;
import com.smartorder.product.ports.outbound.ProductRepositoryPort;
import com.smartorder.product.ports.outbound.ProductSearchPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

@Slf4j
@RequiredArgsConstructor
public class CreateProductService implements CreateProductUseCase {

    private static final Pattern SLUG_NON_ALPHANUM =
            Pattern.compile("[^a-z0-9\\-]");
    private static final Pattern SLUG_MULTI_DASH =
            Pattern.compile("-{2,}");

    private final ProductRepositoryPort  productRepository;
    private final CategoryRepositoryPort categoryRepository;
    private final ProductEventPublisherPort eventPublisher;
    private final ProductSearchPort      searchPort;

    @Override
    public Result execute(Command command) {
        log.debug("Creating product: name={}, sellerId={}",
                command.name(), command.sellerId());

        // ── Validate category exists ─────────────────────────
        categoryRepository.findById(command.categoryId())
                .orElseThrow(() -> new SmartOrderException(
                        ErrorCode.RESOURCE_NOT_FOUND,
                        "categoryId=" + command.categoryId()
                ));

        // ── Guard: SKU uniqueness per seller ─────────────────
        if (command.sku() != null &&
                productRepository.existsBySkuAndSellerId(
                        command.sku(), command.sellerId())) {
            throw new SmartOrderException(
                    ErrorCode.DUPLICATE_RESOURCE,
                    "SKU '" + command.sku() + "' already exists for this seller."
            );
        }

        // ── Build slug ───────────────────────────────────────
        String baseSlug = toSlug(command.name());
        String slug     = ensureUniqueSlug(baseSlug);

        // ── Build money values ───────────────────────────────
        String currency = command.currencyCode() != null
                ? command.currencyCode() : "USD";

        Money price = new Money(command.price(), currency);
        Money compareAtPrice = command.compareAtPrice() != null
                ? new Money(command.compareAtPrice(), currency)
                : null;

        // ── Create domain object ─────────────────────────────
        Product product = new Product(
                command.name(),
                command.description(),
                slug,
                price,
                command.categoryId(),
                command.sellerId(),
                command.sku(),
                command.brand(),
                command.tags()
        );

        if (compareAtPrice != null) {
            product.updateDetails(
                    product.getName(),
                    product.getDescription(),
                    product.getPrice(),
                    compareAtPrice,
                    product.getBrand(),
                    product.getTags()
            );
        }

        // ── Persist ──────────────────────────────────────────
        Product saved = productRepository.save(product);
        log.info("Product created: id={}, slug={}", saved.getId(), saved.getSlug());

        // ── Index in Elasticsearch ───────────────────────────
        try {
            searchPort.indexProduct(saved);
        } catch (Exception e) {
            log.warn("Failed to index product id={}: {}", saved.getId(), e.getMessage());
        }

        // ── Publish domain event ─────────────────────────────
        try {
            eventPublisher.publishProductCreated(saved);
        } catch (Exception e) {
            log.warn("Failed to publish ProductCreated event: {}", e.getMessage());
        }

        return new Result(
                saved.getId().toString(),
                saved.getSlug(),
                saved.getStatus().name()
        );
    }

    // ── Helpers ──────────────────────────────────────────────

    private String toSlug(String input) {
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        String lower = normalized.toLowerCase(Locale.ROOT).trim();
        String dashed = lower.replace(' ', '-');
        String cleaned = SLUG_NON_ALPHANUM.matcher(dashed).replaceAll("");
        return SLUG_MULTI_DASH.matcher(cleaned).replaceAll("-");
    }

    private String ensureUniqueSlug(String base) {
        String candidate = base;
        int    suffix    = 1;
        while (productRepository.existsBySlug(candidate)) {
            candidate = base + "-" + suffix++;
        }
        return candidate;
    }
}