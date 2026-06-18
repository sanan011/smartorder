package com.smartorder.cart.domain.model;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Value object representing a single line item in a cart.
 * Immutable — replaced entirely when quantity changes.
 */
public final class CartItem {

    private final String     productId;
    private final String     productName;
    private final String     productSlug;
    private final String     primaryImageUrl;
    private final BigDecimal unitPrice;
    private final String     currencyCode;
    private final int        quantity;
    private final String     sellerId;

    public CartItem(String productId,
                    String productName,
                    String productSlug,
                    String primaryImageUrl,
                    BigDecimal unitPrice,
                    String currencyCode,
                    int quantity,
                    String sellerId) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Cart item quantity must be positive.");
        }
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Unit price must be non-negative.");
        }
        this.productId      = productId;
        this.productName    = productName;
        this.productSlug    = productSlug;
        this.primaryImageUrl = primaryImageUrl;
        this.unitPrice      = unitPrice;
        this.currencyCode   = currencyCode;
        this.quantity       = quantity;
        this.sellerId       = sellerId;
    }

    public CartItem withQuantity(int newQuantity) {
        return new CartItem(
                productId, productName, productSlug,
                primaryImageUrl, unitPrice, currencyCode,
                newQuantity, sellerId
        );
    }

    public BigDecimal lineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public String     getProductId()       { return productId; }
    public String     getProductName()     { return productName; }
    public String     getProductSlug()     { return productSlug; }
    public String     getPrimaryImageUrl() { return primaryImageUrl; }
    public BigDecimal getUnitPrice()       { return unitPrice; }
    public String     getCurrencyCode()    { return currencyCode; }
    public int        getQuantity()        { return quantity; }
    public String     getSellerId()        { return sellerId; }
}