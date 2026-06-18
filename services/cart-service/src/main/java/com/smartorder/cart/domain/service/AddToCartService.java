package com.smartorder.cart.domain.service;

import com.smartorder.cart.domain.model.Cart;
import com.smartorder.cart.domain.model.CartItem;
import com.smartorder.cart.ports.inbound.AddToCartUseCase;
import com.smartorder.cart.ports.outbound.CartRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class AddToCartService implements AddToCartUseCase {

    private final CartRepositoryPort cartRepository;

    @Override
    public void execute(Command command) {
        // Load or create cart
        Cart cart = cartRepository.findByCartId(command.cartId())
                .orElseGet(() -> new Cart(command.cartId(), command.guest()));

        CartItem item = new CartItem(
                command.productId(),
                command.productName(),
                command.productSlug(),
                command.primaryImageUrl(),
                command.unitPrice(),
                command.currencyCode(),
                command.quantity(),
                command.sellerId()
        );

        cart.addItem(item);
        cartRepository.save(cart);

        log.debug("Added item to cart: cartId={}, productId={}, qty={}",
                command.cartId(), command.productId(), command.quantity());
    }
}