package com.smartorder.cart.domain.service;

import com.smartorder.cart.domain.model.Cart;
import com.smartorder.cart.ports.inbound.GetCartUseCase;
import com.smartorder.cart.ports.outbound.CartRepositoryPort;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GetCartService implements GetCartUseCase {

    private final CartRepositoryPort cartRepository;

    @Override
    public Cart execute(String cartId, boolean guest) {
        return cartRepository.findByCartId(cartId)
                .orElseGet(() -> new Cart(cartId, guest));
    }
}