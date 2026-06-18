package com.smartorder.cart.ports.inbound;

import com.smartorder.cart.domain.model.Cart;

/**
 * Inbound port — retrieve a cart by its ID.
 * Returns an empty cart if none exists yet.
 */
public interface GetCartUseCase {

    Cart execute(String cartId, boolean guest);
}