package com.smartorder.cart.domain.service;

import com.smartorder.cart.domain.model.Cart;
import com.smartorder.cart.ports.inbound.RemoveFromCartUseCase;
import com.smartorder.cart.ports.outbound.CartRepositoryPort;
import com.smartorder.common.exception.ErrorCode;
import com.smartorder.common.exception.SmartOrderException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class RemoveFromCartService implements RemoveFromCartUseCase {

    private final CartRepositoryPort cartRepository;

    @Override
    public void execute(Command command) {
        Cart cart = cartRepository.findByCartId(command.cartId())
                .orElseThrow(() -> new SmartOrderException(
                        ErrorCode.CART_NOT_FOUND, "cartId=" + command.cartId()
                ));

        cart.removeItem(command.productId());
        cartRepository.save(cart);

        log.debug("Removed item from cart: cartId={}, productId={}",
                command.cartId(), command.productId());
    }
}