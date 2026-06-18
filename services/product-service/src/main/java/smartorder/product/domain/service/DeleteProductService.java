package com.smartorder.product.domain.service;

import com.smartorder.common.exception.ErrorCode;
import com.smartorder.common.exception.SmartOrderException;
import com.smartorder.product.domain.model.Product;
import com.smartorder.product.ports.inbound.DeleteProductUseCase;
import com.smartorder.product.ports.outbound.ProductEventPublisherPort;
import com.smartorder.product.ports.outbound.ProductRepositoryPort;
import com.smartorder.product.ports.outbound.ProductSearchPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class DeleteProductService implements DeleteProductUseCase {

    private final ProductRepositoryPort     productRepository;
    private final ProductEventPublisherPort eventPublisher;
    private final ProductSearchPort         searchPort;

    @Override
    public void execute(Command command) {
        Product product = productRepository.findById(command.productId())
                .orElseThrow(() -> new SmartOrderException(
                        ErrorCode.PRODUCT_NOT_FOUND,
                        "productId=" + command.productId()
                ));

        // Non-admin must own the product
        if (!command.isAdmin() && !product.isOwnedBy(command.requesterId())) {
            throw new SmartOrderException(ErrorCode.ACCESS_DENIED);
        }

        product.softDelete();
        productRepository.save(product);
        log.info("Product soft-deleted: id={}", command.productId());

        // Remove from search index
        try {
            searchPort.removeProduct(command.productId().toString());
        } catch (Exception e) {
            log.warn("Failed to remove product from search index: {}", e.getMessage());
        }

        // Publish event
        try {
            eventPublisher.publishProductDeleted(
                    command.productId().toString(),
                    product.getSellerId().toString()
            );
        } catch (Exception e) {
            log.warn("Failed to publish ProductDeleted event: {}", e.getMessage());
        }
    }
}