package com.smartorder.product.ports.inbound;

import java.util.UUID;

/**
 * Inbound port — seller submits a draft product for admin review.
 */
public interface SubmitProductForReviewUseCase {

    void execute(Command command);

    record Command(
            UUID productId,
            UUID sellerId      // validated against product.sellerId
    ) {}
}