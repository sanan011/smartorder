package com.smartorder.cart.adapters.web.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class AddToCartRequest {

    @NotBlank(message = "Product ID is required")
    private String productId;

    @NotBlank(message = "Product name is required")
    private String productName;

    @NotBlank(message = "Product slug is required")
    private String productSlug;

    private String primaryImageUrl;

    @NotNull(message = "Unit price is required")
    @DecimalMin(value = "0.00", message = "Price must be non-negative")
    private BigDecimal unitPrice;

    @NotBlank(message = "Currency code is required")
    @Size(min = 3, max = 3, message = "Currency code must be 3 characters")
    private String currencyCode;

    @Min(value = 1, message = "Quantity must be at least 1")
    @Max(value = 99, message = "Quantity cannot exceed 99")
    private int quantity = 1;

    @NotBlank(message = "Seller ID is required")
    private String sellerId;
}