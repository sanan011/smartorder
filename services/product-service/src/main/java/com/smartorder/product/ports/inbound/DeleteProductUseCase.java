package com.smartorder.product.ports.inbound;

import java.util.UUID;

/**
 * Inbound port — soft-delete a product (seller or admin action).
 */
public interface DeleteProductUseCase {

    void execute(Command command);

    record Command(
            UUID   productId,
            UUID   requesterId,
            boolean isAdmin
    ) {}
}