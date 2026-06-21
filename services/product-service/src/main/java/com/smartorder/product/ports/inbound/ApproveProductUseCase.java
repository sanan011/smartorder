package com.smartorder.product.ports.inbound;

import java.util.UUID;

/**
 * Inbound port — admin approves a pending product listing.
 */
public interface ApproveProductUseCase {

    void execute(Command command);

    record Command(
            UUID   productId,
            UUID   adminId
    ) {}
}