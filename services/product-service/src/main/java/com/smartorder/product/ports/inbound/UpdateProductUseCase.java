package com.smartorder.product.ports.inbound;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Inbound port — update an existing product's details (seller action).
 */
public interface UpdateProductUseCase {

    void execute(Command command);

    record Command(
            UUID         productId,
            UUID         requesterId,    // must match sellerId or be ADMIN
            boolean      isAdmin,        // requester holds the ADMIN role
            String       name,
            String       description,
            BigDecimal   price,
            String       currencyCode,
            BigDecimal   compareAtPrice,
            String       brand,
            List<String> tags
    ) {}
}