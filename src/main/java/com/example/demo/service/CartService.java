package com.example.demo.service;


import com.example.demo.models.CartItems;
import com.example.demo.models.Products;
import com.example.demo.repository.CartItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductService productService;

    public List<CartItems> getCartByUserId(Integer userId) {
        return cartItemRepository.findByUserId(userId);
    }

    public CartItems addToCart(Integer userId, Integer productId, Integer quantity) {
        Products product = productService.getProductById(Long.valueOf(productId));

        if (product.getStock() < quantity) {
            throw new RuntimeException("Not enough stock available");
        }

        // If item already in cart, update quantity
        return cartItemRepository.findByUserIdAndProductId(userId, productId)
                .map(existing -> {
                    int newQty = existing.getQuantity() + quantity;
                    existing.setQuantity(newQty);
                    existing.setTotalAmount(product.getPrice().multiply(BigDecimal.valueOf(newQty)));
                    return cartItemRepository.save(existing);
                })
                .orElseGet(() -> {
                    CartItems item = CartItems.builder()
                            .userId(userId)
                            .productId(productId)
                            .quantity(quantity)
                            .totalAmount(product.getPrice().multiply(BigDecimal.valueOf(quantity)))
                            .build();
                    return cartItemRepository.save(item);
                });
    }

    public CartItems updateCartItem(Integer cartItemId, Integer quantity) {
        CartItems item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found: " + cartItemId));

        Products product = productService.getProductById(Long.valueOf(item.getProductId()));
        item.setQuantity(quantity);
        item.setTotalAmount(product.getPrice().multiply(BigDecimal.valueOf(quantity)));
        return cartItemRepository.save(item);
    }

    public void removeCartItem(Integer cartItemId) {
        cartItemRepository.deleteById(cartItemId);
    }

    public void clearCart(Integer userId) {
        cartItemRepository.deleteByUserId(userId);
    }
}