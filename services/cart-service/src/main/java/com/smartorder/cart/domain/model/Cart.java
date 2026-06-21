package com.smartorder.cart.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

/**
 * Cart aggregate root.
 *
 * A cart is identified by a cartId which can be:
 *  - A userId UUID   (authenticated user)
 *  - A guest token   (anonymous session — generated client-side)
 *
 * Stored entirely in Redis with a configurable TTL.
 * Merged into user cart on login (guest → authenticated).
 */
public class Cart {

    private final String        cartId;       // userId or guestToken
    private final boolean       guest;
    private final Map<String, CartItem> items; // keyed by productId
    private       Instant       createdAt;
    private       Instant       updatedAt;
    private       String        couponCode;
    private       BigDecimal    discountAmount;

    private static final int MAX_ITEMS        = 50;
    private static final int MAX_ITEM_QUANTITY = 99;

    // ── Creation constructor ──────────────────────────────────
    public Cart(String cartId, boolean guest) {
        this.cartId         = cartId;
        this.guest          = guest;
        this.items          = new LinkedHashMap<>();
        this.createdAt      = Instant.now();
        this.updatedAt      = Instant.now();
        this.discountAmount = BigDecimal.ZERO;
    }

    // ── Reconstitution constructor ────────────────────────────
    public Cart(String cartId, boolean guest,
                Map<String, CartItem> items,
                Instant createdAt, Instant updatedAt,
                String couponCode, BigDecimal discountAmount) {
        this.cartId         = cartId;
        this.guest          = guest;
        this.items          = new LinkedHashMap<>(items);
        this.createdAt      = createdAt;
        this.updatedAt      = updatedAt;
        this.couponCode     = couponCode;
        this.discountAmount = discountAmount != null
                ? discountAmount : BigDecimal.ZERO;
    }

    // ── Business rules ────────────────────────────────────────

    /**
     * Adds an item or increases quantity if already present.
     */
    public void addItem(CartItem item) {
        if (items.size() >= MAX_ITEMS && !items.containsKey(item.getProductId())) {
            throw new IllegalStateException(
                    "Cart cannot contain more than " + MAX_ITEMS + " distinct items."
            );
        }

        items.compute(item.getProductId(), (productId, existing) -> {
            if (existing == null) return item;
            int newQty = Math.min(
                    existing.getQuantity() + item.getQuantity(),
                    MAX_ITEM_QUANTITY
            );
            return existing.withQuantity(newQty);
        });

        this.updatedAt = Instant.now();
    }

    /**
     * Updates the quantity of an existing item.
     * Removes the item if quantity is set to 0.
     */
    public void updateItemQuantity(String productId, int quantity) {
        if (!items.containsKey(productId)) {
            throw new NoSuchElementException(
                    "Product not found in cart: " + productId
            );
        }
        if (quantity <= 0) {
            items.remove(productId);
        } else {
            int capped = Math.min(quantity, MAX_ITEM_QUANTITY);
            items.computeIfPresent(productId,
                    (id, item) -> item.withQuantity(capped));
        }
        this.updatedAt = Instant.now();
    }

    /**
     * Removes a single item by productId.
     */
    public void removeItem(String productId) {
        items.remove(productId);
        this.updatedAt = Instant.now();
    }

    /**
     * Clears all items (e.g. after successful order placement).
     */
    public void clear() {
        items.clear();
        this.couponCode     = null;
        this.discountAmount = BigDecimal.ZERO;
        this.updatedAt      = Instant.now();
    }

    /**
     * Merges a guest cart into this (authenticated user) cart.
     * Guest items are added; conflicts resolved by taking higher quantity.
     */
    public void mergeGuestCart(Cart guestCart) {
        guestCart.items.forEach((productId, guestItem) -> {
            // FR-CART-04: don't let a merge push the cart past the 50 distinct-product
            // cap. New products beyond the cap are skipped; existing ones still merge.
            if (!items.containsKey(productId) && items.size() >= MAX_ITEMS) {
                return;
            }
            items.merge(productId, guestItem, (existing, incoming) -> {
                int mergedQty = Math.min(
                        existing.getQuantity() + incoming.getQuantity(),
                        MAX_ITEM_QUANTITY
                );
                return existing.withQuantity(mergedQty);
            });
        });
        this.updatedAt = Instant.now();
    }

    /**
     * Applies a coupon code. Actual discount calculation happens
     * at the order service level — we only store the code here.
     */
    public void applyCoupon(String couponCode) {
        this.couponCode = couponCode;
        this.updatedAt  = Instant.now();
    }

    public void removeCoupon() {
        this.couponCode     = null;
        this.discountAmount = BigDecimal.ZERO;
        this.updatedAt      = Instant.now();
    }

    // ── Computed properties ───────────────────────────────────

    public BigDecimal subtotal() {
        return items.values().stream()
                .map(CartItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal total() {
        return subtotal().subtract(discountAmount);
    }

    public int totalItemCount() {
        return items.values().stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    // ── Getters ───────────────────────────────────────────────

    public String              getCartId()        { return cartId; }
    public boolean             isGuest()          { return guest; }
    public Map<String, CartItem> getItems()       { return Collections.unmodifiableMap(items); }
    public Instant             getCreatedAt()     { return createdAt; }
    public Instant             getUpdatedAt()     { return updatedAt; }
    public String              getCouponCode()    { return couponCode; }
    public BigDecimal          getDiscountAmount(){ return discountAmount; }
}