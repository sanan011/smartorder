package com.smartorder.cart.ports.inbound;

import java.math.BigDecimal;

/**
 * Inbound port — add an item to the cart.
 */
public interface AddToCartUseCase {

    void execute(Command command);

    record Command(
            String     cartId,
            boolean    guest,
            String     productId,
            String     productName,
            String     productSlug,
            String     primaryImageUrl,
            BigDecimal unitPrice,
            String     currencyCode,
            int        quantity,
            String     sellerId
    ) {}
}