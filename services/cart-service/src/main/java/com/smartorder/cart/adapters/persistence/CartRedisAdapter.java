package com.smartorder.cart.adapters.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.smartorder.cart.domain.model.Cart;
import com.smartorder.cart.domain.model.CartItem;
import com.smartorder.cart.ports.outbound.CartRepositoryPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Redis adapter for Cart persistence.
 *
 * Key strategy:
 *   cart:{cartId}  →  JSON-serialised Cart object
 *
 * TTL:
 *   Authenticated carts : 30 days
 *   Guest carts         : 7 days
 */
@Slf4j
@Component
public class CartRedisAdapter implements CartRepositoryPort {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper        objectMapper;

    private static final String   KEY_PREFIX         = "cart:";
    private static final Duration AUTH_CART_TTL      = Duration.ofDays(30);
    private static final Duration GUEST_CART_TTL     = Duration.ofDays(7);

    public CartRedisAdapter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.objectMapper  = new ObjectMapper()
                .registerModule(new JavaTimeModule());
    }

    @Override
    public void save(Cart cart) {
        String key = KEY_PREFIX + cart.getCartId();
        Duration ttl = cart.isGuest() ? GUEST_CART_TTL : AUTH_CART_TTL;

        try {
            String json = objectMapper.writeValueAsString(toSerializable(cart));
            redisTemplate.opsForValue().set(key, json, ttl);
            log.debug("Saved cart: key={}, ttl={}", key, ttl);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialise cart {}: {}", cart.getCartId(), e.getMessage());
            throw new RuntimeException("Cart serialisation failed", e);
        }
    }

    @Override
    public Optional<Cart> findByCartId(String cartId) {
        String key   = KEY_PREFIX + cartId;
        String json  = redisTemplate.opsForValue().get(key);

        if (json == null || json.isBlank()) {
            return Optional.empty();
        }

        try {
            CartDto dto = objectMapper.readValue(json, CartDto.class);
            return Optional.of(fromDto(dto));
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialise cart {}: {}", cartId, e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public void delete(String cartId) {
        redisTemplate.delete(KEY_PREFIX + cartId);
        log.debug("Deleted cart: cartId={}", cartId);
    }

    // ── Serialisation helpers ─────────────────────────────────

    private CartDto toSerializable(Cart cart) {
        CartDto dto = new CartDto();
        dto.cartId         = cart.getCartId();
        dto.guest          = cart.isGuest();
        dto.createdAt      = cart.getCreatedAt().toString();
        dto.updatedAt      = cart.getUpdatedAt().toString();
        dto.couponCode     = cart.getCouponCode();
        dto.discountAmount = cart.getDiscountAmount().toPlainString();

        cart.getItems().forEach((productId, item) -> {
            CartItemDto itemDto = new CartItemDto();
            itemDto.productId       = item.getProductId();
            itemDto.productName     = item.getProductName();
            itemDto.productSlug     = item.getProductSlug();
            itemDto.primaryImageUrl = item.getPrimaryImageUrl();
            itemDto.unitPrice       = item.getUnitPrice().toPlainString();
            itemDto.currencyCode    = item.getCurrencyCode();
            itemDto.quantity        = item.getQuantity();
            itemDto.sellerId        = item.getSellerId();
            dto.items.put(productId, itemDto);
        });

        return dto;
    }

    private Cart fromDto(CartDto dto) {
        Map<String, CartItem> items = new LinkedHashMap<>();
        dto.items.forEach((productId, itemDto) -> {
            items.put(productId, new CartItem(
                    itemDto.productId,
                    itemDto.productName,
                    itemDto.productSlug,
                    itemDto.primaryImageUrl,
                    new java.math.BigDecimal(itemDto.unitPrice),
                    itemDto.currencyCode,
                    itemDto.quantity,
                    itemDto.sellerId
            ));
        });

        return new Cart(
                dto.cartId,
                dto.guest,
                items,
                java.time.Instant.parse(dto.createdAt),
                java.time.Instant.parse(dto.updatedAt),
                dto.couponCode,
                new java.math.BigDecimal(
                        dto.discountAmount != null ? dto.discountAmount : "0"
                )
        );
    }

    // ── Internal DTO classes (Jackson-friendly) ───────────────

    static class CartDto {
        public String                    cartId;
        public boolean                   guest;
        public String                    createdAt;
        public String                    updatedAt;
        public String                    couponCode;
        public String                    discountAmount;
        public Map<String, CartItemDto>  items = new LinkedHashMap<>();
    }

    static class CartItemDto {
        public String productId;
        public String productName;
        public String productSlug;
        public String primaryImageUrl;
        public String unitPrice;
        public String currencyCode;
        public int    quantity;
        public String sellerId;
    }
}