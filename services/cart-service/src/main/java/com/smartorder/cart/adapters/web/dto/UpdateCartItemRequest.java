package com.smartorder.cart.adapters.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateCartItemRequest {

    @Min(value = 0, message = "Quantity must be 0 or greater (0 removes the item)")
    @Max(value = 99, message = "Quantity cannot exceed 99")
    private int quantity;
}