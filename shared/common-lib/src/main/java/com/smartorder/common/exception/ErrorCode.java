package com.smartorder.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Canonical error codes for the entire SmartOrder platform.
 * Each code maps to an HTTP status and a human-readable message.
 */
public enum ErrorCode {

    // ── Generic ─────────────────────────────────────────────
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred."),
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST,               "Request validation failed."),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND,                "The requested resource was not found."),
    DUPLICATE_RESOURCE(HttpStatus.CONFLICT,                 "Resource already exists."),
    OPERATION_NOT_PERMITTED(HttpStatus.FORBIDDEN,           "Operation not permitted."),

    // ── Auth ────────────────────────────────────────────────
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED,            "Invalid username or password."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED,                  "Authentication token has expired."),
    TOKEN_INVALID(HttpStatus.UNAUTHORIZED,                  "Authentication token is invalid."),
    ACCOUNT_DISABLED(HttpStatus.FORBIDDEN,                  "This account has been disabled."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN,                     "You do not have permission to access this resource."),

    // ── User / Seller ───────────────────────────────────────
    USER_NOT_FOUND(HttpStatus.NOT_FOUND,                    "User not found."),
    EMAIL_ALREADY_REGISTERED(HttpStatus.CONFLICT,           "This email address is already registered."),
    SELLER_NOT_VERIFIED(HttpStatus.FORBIDDEN,               "Seller account is pending verification."),

    // ── Product ─────────────────────────────────────────────
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND,                 "Product not found."),
    PRODUCT_INACTIVE(HttpStatus.UNPROCESSABLE_ENTITY,       "Product is no longer available."),
    IMAGE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR,   "Product image upload failed."),

    // ── Inventory ───────────────────────────────────────────
    INSUFFICIENT_STOCK(HttpStatus.CONFLICT,                 "Insufficient stock to fulfil the request."),
    INVENTORY_LOCK_FAILED(HttpStatus.CONFLICT,              "Could not acquire inventory lock. Please retry."),

    // ── Cart ────────────────────────────────────────────────
    CART_NOT_FOUND(HttpStatus.NOT_FOUND,                    "Shopping cart not found."),
    CART_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND,               "Item not found in cart."),

    // ── Order / SAGA ────────────────────────────────────────
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND,                   "Order not found."),
    ORDER_ALREADY_CONFIRMED(HttpStatus.CONFLICT,            "Order has already been confirmed."),
    ORDER_CANCELLATION_FAILED(HttpStatus.UNPROCESSABLE_ENTITY, "Order cannot be cancelled in its current state."),
    PAYMENT_FAILED(HttpStatus.PAYMENT_REQUIRED,             "Payment processing failed."),
    SAGA_COMPENSATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Saga compensation step failed. Manual review required.");

    private final HttpStatus httpStatus;
    private final String     defaultMessage;

    ErrorCode(HttpStatus httpStatus, String defaultMessage) {
        this.httpStatus     = httpStatus;
        this.defaultMessage = defaultMessage;
    }

    public HttpStatus getHttpStatus()     { return httpStatus;     }
    public String     getDefaultMessage() { return defaultMessage; }
}