package com.smartorder.cart.adapters.web;

import com.smartorder.cart.adapters.web.dto.AddToCartRequest;
import com.smartorder.cart.adapters.web.dto.CartResponse;
import com.smartorder.cart.adapters.web.dto.UpdateCartItemRequest;
import com.smartorder.cart.domain.model.Cart;
import com.smartorder.cart.ports.inbound.*;
import com.smartorder.common.exception.ErrorCode;
import com.smartorder.common.exception.SmartOrderException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST adapter for cart endpoints.
 *
 * Cart ID resolution:
 *   - Authenticated: X-Auth-User-Id header (set by Gateway JWT filter)
 *   - Guest:         X-Guest-Cart-Id header (generated and stored client-side)
 *
 * All routes: /api/v1/cart
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

    private final GetCartUseCase        getCartUseCase;
    private final AddToCartUseCase      addToCartUseCase;
    private final UpdateCartItemUseCase updateCartItemUseCase;
    private final RemoveFromCartUseCase removeFromCartUseCase;
    private final MergeCartUseCase      mergeCartUseCase;
    private final ClearCartUseCase      clearCartUseCase;

    // ── GET /api/v1/cart ──────────────────────────────────────

    @GetMapping
    public ResponseEntity<CartResponse> getCart(HttpServletRequest request) {
        CartContext ctx  = resolveCart(request);
        Cart        cart = getCartUseCase.execute(ctx.cartId(), ctx.guest());
        return ResponseEntity.ok(CartResponse.from(cart));
    }

    // ── POST /api/v1/cart/items ───────────────────────────────

    @PostMapping("/items")
    public ResponseEntity<CartResponse> addItem(
            @Valid @RequestBody AddToCartRequest body,
            HttpServletRequest request) {

        CartContext ctx = resolveCart(request);

        addToCartUseCase.execute(new AddToCartUseCase.Command(
                ctx.cartId(),
                ctx.guest(),
                body.getProductId(),
                body.getProductName(),
                body.getProductSlug(),
                body.getPrimaryImageUrl(),
                body.getUnitPrice(),
                body.getCurrencyCode(),
                body.getQuantity(),
                body.getSellerId()
        ));

        Cart updated = getCartUseCase.execute(ctx.cartId(), ctx.guest());
        return ResponseEntity.ok(CartResponse.from(updated));
    }

    // ── PATCH /api/v1/cart/items/{productId} ──────────────────

    @PatchMapping("/items/{productId}")
    public ResponseEntity<CartResponse> updateItem(
            @PathVariable String productId,
            @Valid @RequestBody UpdateCartItemRequest body,
            HttpServletRequest request) {

        CartContext ctx = resolveCart(request);

        updateCartItemUseCase.execute(new UpdateCartItemUseCase.Command(
                ctx.cartId(), productId, body.getQuantity()
        ));

        Cart updated = getCartUseCase.execute(ctx.cartId(), ctx.guest());
        return ResponseEntity.ok(CartResponse.from(updated));
    }

    // ── DELETE /api/v1/cart/items/{productId} ─────────────────

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<CartResponse> removeItem(
            @PathVariable String productId,
            HttpServletRequest request) {

        CartContext ctx = resolveCart(request);

        removeFromCartUseCase.execute(new RemoveFromCartUseCase.Command(
                ctx.cartId(), productId
        ));

        Cart updated = getCartUseCase.execute(ctx.cartId(), ctx.guest());
        return ResponseEntity.ok(CartResponse.from(updated));
    }

    // ── DELETE /api/v1/cart ───────────────────────────────────

    @DeleteMapping
    public ResponseEntity<Void> clearCart(HttpServletRequest request) {
        CartContext ctx = resolveCart(request);
        clearCartUseCase.execute(ctx.cartId());
        return ResponseEntity.noContent().build();
    }

    // ── POST /api/v1/cart/merge ───────────────────────────────

    @PostMapping("/merge")
    public ResponseEntity<CartResponse> mergeGuestCart(
            @RequestBody Map<String, String> body,
            HttpServletRequest request) {

        String userId      = request.getHeader("X-Auth-User-Id");
        String guestCartId = body.get("guestCartId");

        if (userId == null || userId.isBlank()) {
            throw new SmartOrderException(ErrorCode.ACCESS_DENIED);
        }
        if (guestCartId == null || guestCartId.isBlank()) {
            throw new SmartOrderException(
                    ErrorCode.VALIDATION_FAILED, "guestCartId is required."
            );
        }

        mergeCartUseCase.execute(new MergeCartUseCase.Command(userId, guestCartId));

        Cart merged = getCartUseCase.execute(userId, false);
        return ResponseEntity.ok(CartResponse.from(merged));
    }

    // ── Helpers ───────────────────────────────────────────────

    private CartContext resolveCart(HttpServletRequest request) {
        String userId      = request.getHeader("X-Auth-User-Id");
        String guestCartId = request.getHeader("X-Guest-Cart-Id");

        if (userId != null && !userId.isBlank()) {
            return new CartContext(userId, false);
        }
        if (guestCartId != null && !guestCartId.isBlank()) {
            return new CartContext(guestCartId, true);
        }
        throw new SmartOrderException(
                ErrorCode.VALIDATION_FAILED,
                "Either X-Auth-User-Id or X-Guest-Cart-Id header is required."
        );
    }

    private record CartContext(String cartId, boolean guest) {}
}