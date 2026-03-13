package com.example.demo.service;

import com.example.demo.exception.NotFoundException;
import com.example.demo.models.CartItems;
import com.example.demo.models.Products;
import com.example.demo.models.Users;
import com.example.demo.models.dtos.AddToCartRequest;
import com.example.demo.models.dtos.CartResponse;
import com.example.demo.repository.CartItemRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final SmsService smsService;

    // ── Helper: get authenticated user ────────────────────────────────────────
    private Users getAuthenticatedUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Authenticated user not found"));
    }

    // ── GET cart ───────────────────────────────────────────────────────────────
    public CartResponse getCart() {
        Users user = getAuthenticatedUser();
        List<CartItems> items = cartItemRepository.findByUser(user);
        return buildCartResponse(items);
    }

    // ── POST add to cart ───────────────────────────────────────────────────────
    @Transactional
    public CartResponse addToCart(AddToCartRequest request) {
        Users user = getAuthenticatedUser();

        Products product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new NotFoundException("Product not found with id: " + request.getProductId()));

        // 400 if out of stock
        if (product.getStock() <= 0) {
            throw new IllegalArgumentException("Product '" + product.getName() + "' is out of stock");
        }

        // Check if already in cart — if so, increase quantity
        CartItems cartItem = cartItemRepository.findByUserAndProduct(user, product)
                .map(existing -> {
                    int newQty = existing.getQuantity() + request.getQuantity();
                    // 400 if quantity exceeds available stock
                    if (newQty > product.getStock()) {
                        throw new IllegalArgumentException(
                                "Requested quantity (" + newQty + ") exceeds available stock (" + product.getStock() + ")");
                    }
                    existing.setQuantity(newQty);
                    return cartItemRepository.save(existing);
                })
                .orElseGet(() -> {
                    // 400 if initial quantity exceeds stock
                    if (request.getQuantity() > product.getStock()) {
                        throw new IllegalArgumentException(
                                "Requested quantity (" + request.getQuantity() + ") exceeds available stock (" + product.getStock() + ")");
                    }
                    CartItems newItem = CartItems.builder()
                            .user(user)
                            .product(product)
                            .quantity(request.getQuantity())
                            .build();
                    return cartItemRepository.save(newItem);
                });

        List<CartItems> allItems = cartItemRepository.findByUser(user);
        CartResponse cartResponse = buildCartResponse(allItems);

        // SMS the cart summary to the customer
        smsService.sendCartSummary(
                user.getMobile(),
                user.getName(),
                allItems,
                cartResponse.getCartTotal()
        );

        return cartResponse;
    }

    // ── PATCH update item quantity (quantity=0 removes item) ───────────────────
    @Transactional
    public CartResponse updateCartItem(Integer itemId, Integer quantity) {
        Users user = getAuthenticatedUser();

        CartItems item = cartItemRepository.findByIdAndUser(itemId, user)
                .orElseThrow(() -> new NotFoundException("Cart item not found or does not belong to you"));

        if (quantity == 0) {
            // quantity=0 means remove
            cartItemRepository.delete(item);
        } else {
            // Validate against current stock (always uses live stock)
            Products product = item.getProduct();
            if (quantity > product.getStock()) {
                throw new IllegalArgumentException(
                        "Requested quantity (" + quantity + ") exceeds available stock (" + product.getStock() + ")");
            }
            item.setQuantity(quantity);
            cartItemRepository.save(item);
        }

        List<CartItems> allItems = cartItemRepository.findByUser(user);
        return buildCartResponse(allItems);
    }

    // ── DELETE clear entire cart ───────────────────────────────────────────────
    @Transactional
    public void clearCart() {
        Users user = getAuthenticatedUser();
        List<CartItems> items = cartItemRepository.findByUser(user);
        if (items.isEmpty()) {
            throw new NotFoundException("No cart found for this user");
        }
        cartItemRepository.deleteByUser(user);
    }

    // ── Build response (always uses live product prices) ──────────────────────
    private CartResponse buildCartResponse(List<CartItems> items) {
        List<CartResponse.CartItemResponse> itemResponses = items.stream()
                .map(item -> CartResponse.CartItemResponse.builder()
                        .id(item.getId())
                        .product(item.getProduct())
                        .quantity(item.getQuantity())
                        .lineTotal(item.getLineTotal()) // live price * quantity
                        .build())
                .toList();

        BigDecimal cartTotal = itemResponses.stream()
                .map(CartResponse.CartItemResponse::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartResponse.builder()
                .items(itemResponses)
                .cartTotal(cartTotal)
                .build();
    }
}