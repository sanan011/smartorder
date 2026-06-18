package com.smartorder.cart.domain.service;

import com.smartorder.cart.domain.model.Cart;
import com.smartorder.cart.ports.inbound.MergeCartUseCase;
import com.smartorder.cart.ports.outbound.CartRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class MergeCartService implements MergeCartUseCase {

    private final CartRepositoryPort cartRepository;

    @Override
    public void execute(Command command) {
        // Load or create the authenticated user cart
        Cart userCart = cartRepository.findByCartId(command.userId())
                .orElseGet(() -> new Cart(command.userId(), false));

        // Load guest cart — if absent nothing to merge
        cartRepository.findByCartId(command.guestCartId()).ifPresent(guestCart -> {
            userCart.mergeGuestCart(guestCart);
            cartRepository.save(userCart);
            // Delete the guest cart after merging
            cartRepository.delete(command.guestCartId());
            log.info("Merged guest cart {} into user cart {}",
                    command.guestCartId(), command.userId());
        });
    }
}