package com.smartorder.cart.ports.inbound;

/**
 * Inbound port — merge a guest cart into an authenticated user cart.
 * Called immediately after login on the frontend.
 */
public interface MergeCartUseCase {

    void execute(Command command);

    record Command(
            String userId,       // target authenticated cart
            String guestCartId   // source guest cart to merge and delete
    ) {}
}