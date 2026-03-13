package com.example.demo.controller;

import com.example.demo.models.dtos.AddToCartRequest;
import com.example.demo.models.dtos.CartResponse;
import com.example.demo.models.dtos.UpdateCartRequest;
import com.example.demo.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    // GET /api/cart — get authenticated user's cart
    @GetMapping
    public ResponseEntity<CartResponse> getCart() {
        return ResponseEntity.ok(cartService.getCart());
    }

    // POST /api/cart — add item to cart
    @PostMapping
    public ResponseEntity<CartResponse> addToCart(@RequestBody @Valid AddToCartRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cartService.addToCart(request));
    }

    // PATCH /api/cart/:itemId — update quantity (0 = remove)
    @PatchMapping("/{itemId}")
    public ResponseEntity<CartResponse> updateCartItem(
            @PathVariable Integer itemId,
            @RequestBody @Valid UpdateCartRequest request) {
        return ResponseEntity.ok(cartService.updateCartItem(itemId, request.getQuantity()));
    }

    // DELETE /api/cart — clear entire cart
    @DeleteMapping
    public ResponseEntity<Void> clearCart() {
        cartService.clearCart();
        return ResponseEntity.noContent().build();
    }
}