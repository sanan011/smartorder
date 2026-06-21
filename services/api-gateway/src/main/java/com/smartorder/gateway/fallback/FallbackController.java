package com.smartorder.gateway.fallback;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

/**
 * Circuit breaker fallback endpoints.
 * Returns a structured 503 response instead of an ugly Netty error
 * when a downstream service is unavailable.
 */
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @RequestMapping("/auth")
    public ResponseEntity<Map<String, Object>> authFallback() {
        return buildFallback("auth-service");
    }

    @RequestMapping("/product")
    public ResponseEntity<Map<String, Object>> productFallback() {
        return buildFallback("product-service");
    }

    @RequestMapping("/inventory")
    public ResponseEntity<Map<String, Object>> inventoryFallback() {
        return buildFallback("inventory-service");
    }

    @RequestMapping("/cart")
    public ResponseEntity<Map<String, Object>> cartFallback() {
        return buildFallback("cart-service");
    }

    @RequestMapping("/order")
    public ResponseEntity<Map<String, Object>> orderFallback() {
        return buildFallback("order-service");
    }

    @RequestMapping("/seller")
    public ResponseEntity<Map<String, Object>> sellerFallback() {
        return buildFallback("seller-service");
    }

    @RequestMapping("/notification")
    public ResponseEntity<Map<String, Object>> notificationFallback() {
        return buildFallback("notification-service");
    }

    private ResponseEntity<Map<String, Object>> buildFallback(String serviceName) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
                "timestamp",  Instant.now().toString(),
                "status",     503,
                "errorCode",  "SERVICE_UNAVAILABLE",
                "message",    "The " + serviceName + " is temporarily unavailable. Please try again shortly.",
                "service",    serviceName
        ));
    }
}