package com.smartorder.product.domain.service;

import com.smartorder.common.exception.ErrorCode;
import com.smartorder.common.exception.SmartOrderException;
import com.smartorder.product.domain.model.Product;
import com.smartorder.product.ports.inbound.ApproveProductUseCase;
import com.smartorder.product.ports.inbound.RejectProductUseCase;
import com.smartorder.product.ports.inbound.SubmitProductForReviewUseCase;
import com.smartorder.product.ports.outbound.ProductEventPublisherPort;
import com.smartorder.product.ports.outbound.ProductRepositoryPort;
import com.smartorder.product.ports.outbound.ProductSearchPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Handles the product approval workflow:
 * submit → pending → approve/reject
 *
 * One service class implements three related use cases
 * to avoid scattering tightly coupled workflow logic.
 */
@Slf4j
@RequiredArgsConstructor
public class ReviewProductService implements
        SubmitProductForReviewUseCase,
        ApproveProductUseCase,
        RejectProductUseCase {

    private final ProductRepositoryPort     productRepository;
    private final ProductEventPublisherPort eventPublisher;
    private final ProductSearchPort         searchPort;

    // ── Submit for review (seller) ────────────────────────────

    @Override
    public void execute(SubmitProductForReviewUseCase.Command command) {
        Product product = loadProduct(command.productId());

        if (!product.isOwnedBy(command.sellerId())) {
            throw new SmartOrderException(ErrorCode.ACCESS_DENIED);
        }

        try {
            product.submitForReview();
        } catch (IllegalStateException e) {
            throw new SmartOrderException(
                    ErrorCode.OPERATION_NOT_PERMITTED, e.getMessage()
            );
        }

        productRepository.save(product);
        log.info("Product submitted for review: id={}", command.productId());
    }

    // ── Approve (admin) ───────────────────────────────────────

    @Override
    public void execute(ApproveProductUseCase.Command command) {
        Product product = loadProduct(command.productId());

        try {
            product.approve();
        } catch (IllegalStateException e) {
            throw new SmartOrderException(
                    ErrorCode.OPERATION_NOT_PERMITTED, e.getMessage()
            );
        }

        Product saved = productRepository.save(product);
        log.info("Product approved by admin={}: productId={}",
                command.adminId(), command.productId());

        // Update search index with ACTIVE status
        try {
            searchPort.indexProduct(saved);
        } catch (Exception e) {
            log.warn("Failed to re-index approved product: {}", e.getMessage());
        }

        try {
            eventPublisher.publishProductApproved(saved);
        } catch (Exception e) {
            log.warn("Failed to publish ProductApproved event: {}", e.getMessage());
        }
    }

    // ── Reject (admin) ────────────────────────────────────────

    @Override
    public void execute(RejectProductUseCase.Command command) {
        if (command.reason() == null || command.reason().isBlank()) {
            throw new SmartOrderException(
                    ErrorCode.VALIDATION_FAILED,
                    "Rejection reason is required."
            );
        }

        Product product = loadProduct(command.productId());

        try {
            product.reject(command.reason());
        } catch (IllegalStateException e) {
            throw new SmartOrderException(
                    ErrorCode.OPERATION_NOT_PERMITTED, e.getMessage()
            );
        }

        productRepository.save(product);
        log.info("Product rejected by admin={}: productId={}, reason={}",
                command.adminId(), command.productId(), command.reason());

        try {
            eventPublisher.publishProductRejected(product, command.reason());
        } catch (Exception e) {
            log.warn("Failed to publish ProductRejected event: {}", e.getMessage());
        }
    }

    // ── Shared ────────────────────────────────────────────────

    private Product loadProduct(java.util.UUID productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new SmartOrderException(
                        ErrorCode.PRODUCT_NOT_FOUND,
                        "productId=" + productId
                ));
    }
}