package com.example.demo.models.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateCartRequest {
    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity must be 0 or more (0 removes the item)")
    private Integer quantity;
}
