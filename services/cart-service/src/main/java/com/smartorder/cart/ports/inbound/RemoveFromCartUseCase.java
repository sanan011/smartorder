package com.smartorder.cart.ports.inbound;

/**
 * Inbound port — remove a specific item from the cart.
 */
public interface RemoveFromCartUseCase {

    void execute(Command command);

    record Command(
            String cartId,
            String productId
    ) {}
}