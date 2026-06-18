package com.smartorder.cart.adapters.web.dto;

import com.smartorder.cart.domain.model.Cart;
import com.smartorder.cart.domain.model.CartItem;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class CartResponse {

    private final String          cartId;
    private final boolean         guest;
    private final List<ItemDto>   items;
    private final int             totalItemCount;
    private final BigDecimal      subtotal;
    private final BigDecimal      discountAmount;
    private final BigDecimal      total;
    private final String          couponCode;
    private final String          currencyCode;

    @Getter
    @Builder
    public static class ItemDto {
        private final String     productId;
        private final String     productName;
        private final String     productSlug;
        private final String     primaryImageUrl;
        private final BigDecimal unitPrice;
        private final String     currencyCode;
        private final int        quantity;
        private final BigDecimal lineTotal;
        private final String     sellerId;
    }

    public static CartResponse from(Cart cart) {
        List<ItemDto> items = cart.getItems().values().stream()
                .map(item -> ItemDto.builder()
                        .productId(item.getProductId())
                        .productName(item.getProductName())
                        .productSlug(item.getProductSlug())
                        .primaryImageUrl(item.getPrimaryImageUrl())
                        .unitPrice(item.getUnitPrice())
                        .currencyCode(item.getCurrencyCode())
                        .quantity(item.getQuantity())
                        .lineTotal(item.lineTotal())
                        .sellerId(item.getSellerId())
                        .build())
                .collect(Collectors.toList());

        // Use currency of first item (all items assumed same currency)
        String currency = cart.getItems().values().stream()
                .findFirst()
                .map(CartItem::getCurrencyCode)
                .orElse("USD");

        return CartResponse.builder()
                .cartId(cart.getCartId())
                .guest(cart.isGuest())
                .items(items)
                .totalItemCount(cart.totalItemCount())
                .subtotal(cart.subtotal())
                .discountAmount(cart.getDiscountAmount())
                .total(cart.total())
                .couponCode(cart.getCouponCode())
                .currencyCode(currency)
                .build();
    }
}