package com.smartorder.cart.domain.service;

import com.smartorder.cart.domain.model.Cart;
import com.smartorder.cart.ports.inbound.UpdateCartItemUseCase;
import com.smartorder.cart.ports.outbound.CartRepositoryPort;
import com.smartorder.common.exception.ErrorCode;
import com.smartorder.common.exception.SmartOrderException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class UpdateCartItemService implements UpdateCartItemUseCase {

    private final CartRepositoryPort cartRepository;

    @Override
    public void execute(Command command) {
        Cart cart = cartRepository.findByCartId(command.cartId())
                .orElseThrow(() -> new SmartOrderException(
                        ErrorCode.CART_NOT_FOUND, "cartId=" + command.cartId()
                ));

        try {
            cart.updateItemQuantity(command.productId(), command.quantity());
        } catch (java.util.NoSuchElementException e) {
            throw new SmartOrderException(
                    ErrorCode.CART_ITEM_NOT_FOUND,
                    "productId=" + command.productId()
            );
        }

        cartRepository.save(cart);
        log.debug("Updated cart item: cartId={}, productId={}, qty={}",
                command.cartId(), command.productId(), command.quantity());
    }
}