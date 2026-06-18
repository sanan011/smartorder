package com.smartorder.product.domain.service;

import com.smartorder.common.exception.ErrorCode;
import com.smartorder.common.exception.SmartOrderException;
import com.smartorder.product.domain.model.Product;
import com.smartorder.product.domain.model.ProductStatus;
import com.smartorder.product.ports.inbound.GetProductUseCase;
import com.smartorder.product.ports.outbound.ProductRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class GetProductService implements GetProductUseCase {

    private final ProductRepositoryPort productRepository;

    @Override
    public Product findById(UUID productId) {
        return productRepository.findById(productId)
                .filter(p -> p.getStatus() != ProductStatus.DELETED)
                .orElseThrow(() -> new SmartOrderException(
                        ErrorCode.PRODUCT_NOT_FOUND,
                        "productId=" + productId
                ));
    }

    @Override
    public Product findBySlug(String slug) {
        return productRepository.findBySlug(slug)
                .filter(p -> p.getStatus() != ProductStatus.DELETED)
                .orElseThrow(() -> new SmartOrderException(
                        ErrorCode.PRODUCT_NOT_FOUND,
                        "slug=" + slug
                ));
    }

    @Override
    public List<Product> findByCategory(UUID categoryId, int page, int size) {
        return productRepository.findByCategoryId(categoryId, page, size);
    }

    @Override
    public List<Product> findBySeller(UUID sellerId,
                                      String status,
                                      int page,
                                      int size) {
        ProductStatus productStatus = parseStatus(status);
        return productRepository.findBySellerId(sellerId, productStatus, page, size);
    }

    private ProductStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return ProductStatus.ACTIVE;
        }
        try {
            return ProductStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new SmartOrderException(
                    ErrorCode.VALIDATION_FAILED,
                    "Invalid product status: " + status
            );
        }
    }
}