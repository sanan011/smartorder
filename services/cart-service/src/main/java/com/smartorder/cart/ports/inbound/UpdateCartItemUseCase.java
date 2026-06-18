package com.smartorder.cart.ports.inbound;

/**
 * Inbound port — update the quantity of a cart item.
 * Quantity of 0 removes the item.
 */
public interface UpdateCartItemUseCase {

    void execute(Command command);

    record Command(
            String cartId,
            String productId,
            int    quantity
    ) {}
}