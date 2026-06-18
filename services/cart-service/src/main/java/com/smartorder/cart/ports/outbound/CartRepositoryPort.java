package com.smartorder.cart.ports.outbound;

import com.smartorder.cart.domain.model.Cart;

import java.util.Optional;

/**
 * Outbound port — Redis persistence for Cart aggregate.
 */
public interface CartRepositoryPort {

    /**
     * Persists the cart with a TTL.
     * Authenticated carts: 30 days.
     * Guest carts: 7 days.
     */
    void save(Cart cart);

    Optional<Cart> findByCartId(String cartId);

    void delete(String cartId);
}