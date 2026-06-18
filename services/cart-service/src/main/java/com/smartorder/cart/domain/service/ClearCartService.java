package com.smartorder.cart.domain.service;

import com.smartorder.cart.domain.model.Cart;
import com.smartorder.cart.ports.inbound.ClearCartUseCase;
import com.smartorder.cart.ports.outbound.CartRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class ClearCartService implements ClearCartUseCase {

    private final CartRepositoryPort cartRepository;

    @Override
    public void execute(String cartId) {
        cartRepository.findByCartId(cartId).ifPresent(cart -> {
            cart.clear();
            cartRepository.save(cart);
            log.info("Cart cleared: cartId={}", cartId);
        });
    }
}