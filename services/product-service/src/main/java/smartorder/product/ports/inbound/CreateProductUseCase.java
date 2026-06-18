package com.smartorder.product.ports.inbound;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Inbound port — create a new product listing (seller action).
 */
public interface CreateProductUseCase {

    Result execute(Command command);

    record Command(
            String       name,
            String       description,
            BigDecimal   price,
            String       currencyCode,
            BigDecimal   compareAtPrice,  // nullable
            UUID         categoryId,
            UUID         sellerId,
            String       sku,
            String       brand,
            List<String> tags
    ) {}

    record Result(
            String productId,
            String slug,
            String status
    ) {}
}