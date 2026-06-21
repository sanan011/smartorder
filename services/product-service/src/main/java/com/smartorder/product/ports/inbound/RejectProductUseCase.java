package com.smartorder.product.ports.inbound;

import java.util.UUID;

/**
 * Inbound port — admin rejects a pending product with a reason.
 */
public interface RejectProductUseCase {

    void execute(Command command);

    record Command(
            UUID   productId,
            UUID   adminId,
            String reason
    ) {}
}