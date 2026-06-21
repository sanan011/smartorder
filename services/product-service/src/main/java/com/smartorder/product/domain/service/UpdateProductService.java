package com.smartorder.product.domain.service;

import com.smartorder.common.exception.ErrorCode;
import com.smartorder.common.exception.SmartOrderException;
import com.smartorder.product.domain.model.Money;
import com.smartorder.product.domain.model.Product;
import com.smartorder.product.ports.inbound.UpdateProductUseCase;
import com.smartorder.product.ports.outbound.ProductEventPublisherPort;
import com.smartorder.product.ports.outbound.ProductRepositoryPort;
import com.smartorder.product.ports.outbound.ProductSearchPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class UpdateProductService implements UpdateProductUseCase {

    private final ProductRepositoryPort     productRepository;
    private final ProductEventPublisherPort eventPublisher;
    private final ProductSearchPort         searchPort;

    @Override
    public void execute(Command command) {
        // ── Load product ─────────────────────────────────────
        Product product = productRepository.findById(command.productId())
                .orElseThrow(() -> new SmartOrderException(
                        ErrorCode.PRODUCT_NOT_FOUND,
                        "productId=" + command.productId()
                ));

        // ── Ownership check ──────────────────────────────────
        if (!command.isAdmin() && !product.isOwnedBy(command.requesterId())) {
            throw new SmartOrderException(ErrorCode.ACCESS_DENIED);
        }

        // ── Build money values ───────────────────────────────
        String currency = command.currencyCode() != null
                ? command.currencyCode()
                : product.getPrice().getCurrencyCode();

        Money newPrice = new Money(command.price(), currency);
        Money newCompareAt = command.compareAtPrice() != null
                ? new Money(command.compareAtPrice(), currency)
                : null;

        // ── Apply domain update ──────────────────────────────
        product.updateDetails(
                command.name(),
                command.description(),
                newPrice,
                newCompareAt,
                command.brand(),
                command.tags()
        );

        // ── Persist ──────────────────────────────────────────
        Product saved = productRepository.save(product);
        log.info("Product updated: id={}", saved.getId());

        // ── Re-index ─────────────────────────────────────────
        try {
            searchPort.indexProduct(saved);
        } catch (Exception e) {
            log.warn("Failed to re-index product id={}: {}", saved.getId(), e.getMessage());
        }

        // ── Publish event ─────────────────────────────────────
        try {
            eventPublisher.publishProductUpdated(saved);
        } catch (Exception e) {
            log.warn("Failed to publish ProductUpdated event: {}", e.getMessage());
        }
    }
}