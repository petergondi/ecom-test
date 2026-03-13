package com.example.demo.models.dtos;

import com.example.demo.models.Products;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class CartResponse {
    private List<CartItemResponse> items;
    private BigDecimal cartTotal;

    @Data
    @Builder
    public static class CartItemResponse {
        private Integer id;
        private Products product;
        private Integer quantity;
        private BigDecimal lineTotal;
    }
}