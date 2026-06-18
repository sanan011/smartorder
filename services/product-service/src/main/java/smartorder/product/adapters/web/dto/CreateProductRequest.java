package com.smartorder.product.adapters.web.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor
public class CreateProductRequest {

    @NotBlank(message = "Product name is required")
    @Size(max = 255)
    private String name;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private BigDecimal price;

    private String currencyCode = "USD";

    private BigDecimal compareAtPrice;

    @NotNull(message = "Category is required")
    private UUID categoryId;

    @Size(max = 100)
    private String sku;

    @Size(max = 150)
    private String brand;

    private List<String> tags;
}