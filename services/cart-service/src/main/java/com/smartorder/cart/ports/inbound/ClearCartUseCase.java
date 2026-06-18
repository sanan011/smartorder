package com.smartorder.cart.ports.inbound;

/**
 * Inbound port — clear all items from a cart.
 * Called by the Order Service after successful order placement.
 */
public interface ClearCartUseCase {

    void execute(String cartId);
}