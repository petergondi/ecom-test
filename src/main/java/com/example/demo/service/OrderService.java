package com.example.demo.service;

import com.example.demo.enums.OrderStatus;
import com.example.demo.exception.NotFoundException;
import com.example.demo.exception.StockConflictException;
import com.example.demo.models.*;
import com.example.demo.models.dtos.CheckoutRequest;
import com.example.demo.models.dtos.OrderResponse;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final SmsService smsService;

    // ── Helper: get authenticated user
    private Users getAuthenticatedUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Authenticated user not found"));
    }

    // ── Helper: map Orders entity → OrderResponse DTO
    private OrderResponse toResponse(Orders order) {
        List<OrderResponse.OrderItemResponse> itemResponses = order.getItems().stream()
                .map(item -> OrderResponse.OrderItemResponse.builder()
                        .id(item.getId())
                        .productName(item.getProduct().getName())
                        .productCategory(item.getProduct().getCategory())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .lineTotal(item.getLineTotal())
                        .build())
                .toList();

        return OrderResponse.builder()
                .id(order.getId())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .createdAt(order.getCreatedAt())
                .items(itemResponses)
                .build();
    }

    // ── POST /orders/checkout
    @Transactional
    public OrderResponse checkout(CheckoutRequest request) {
        Users user = getAuthenticatedUser();

        // 1. Resolve idempotency key
        // If client didn't supply one, generate a 10-second bucket key
        String idempotencyKey = (request != null && request.getIdempotencyKey() != null
                && !request.getIdempotencyKey().isBlank())
                ? request.getIdempotencyKey()
                : user.getId() + ":" + (System.currentTimeMillis() / 10000); // 10s bucket

        // 2. Idempotency check — return existing order if found
        LocalDateTime tenSecondsAgo = LocalDateTime.now().minusSeconds(10);
        var existing = orderRepository.findRecentByUserAndIdempotencyKey(
                user, idempotencyKey, tenSecondsAgo);
        if (existing.isPresent()) {
            log.info("Idempotent checkout for user {} with key {}", user.getId(), idempotencyKey);
            return toResponse(existing.get());
        }

        // ── 3. Verify cart is not empty
        List<CartItems> cartItems = cartItemRepository.findByUser(user);
        if (cartItems.isEmpty()) {
            throw new IllegalArgumentException("Your cart is empty");
        }

        // ── 4. Verify stock for ALL items before making any changes
        List<String> stockErrors = new ArrayList<>();
        for (CartItems cartItem : cartItems) {
            Products product = productRepository.findById(cartItem.getProduct().getId())
                    .orElseThrow(() -> new NotFoundException(
                            "Product not found: " + cartItem.getProduct().getName()));

            if (product.getStock() < cartItem.getQuantity()) {
                stockErrors.add(String.format(
                        "'%s' — requested: %d, available: %d",
                        product.getName(), cartItem.getQuantity(), product.getStock()));
            }
        }
        // 409 if any stock check failed — nothing has been modified yet
        if (!stockErrors.isEmpty()) {
            throw new StockConflictException(stockErrors);
        }

        // ─5. Create order
        Orders order = Orders.builder()
                .user(user)
                .status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.ZERO) // updated below
                .idempotencyKey(idempotencyKey)
                .build();
        Orders savedOrder = orderRepository.save(order);

        // ── 6. Create order items, deduct stock
        BigDecimal total = BigDecimal.ZERO;
        List<OrderItems> orderItems = new ArrayList<>();

        for (CartItems cartItem : cartItems) {
            Products product = productRepository.findById(cartItem.getProduct().getId()).get();

            // Lock the price at time of purchase
            BigDecimal lockedUnitPrice = product.getPrice();

            OrderItems orderItem = OrderItems.builder()
                    .order(savedOrder)
                    .product(product)
                    .quantity(cartItem.getQuantity())
                    .unitPrice(lockedUnitPrice) // never recalculated
                    .build();
            orderItems.add(orderItem);

            // Deduct stock
            product.setStock(product.getStock() - cartItem.getQuantity());
            productRepository.save(product);

            total = total.add(lockedUnitPrice.multiply(BigDecimal.valueOf(cartItem.getQuantity())));
        }

        orderItemRepository.saveAll(orderItems);

        // ── 7. Update order total
        savedOrder.setTotalAmount(total);
        savedOrder.setItems(orderItems);
        orderRepository.save(savedOrder);

        // ── 8. Clear cart
        cartItemRepository.deleteByUser(user);

        // ── 9. SMS confirmation
        sendOrderConfirmationSms(user, savedOrder, orderItems);

        return toResponse(savedOrder);
    }

    // ── GET /orders — order history
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrderHistory() {
        Users user = getAuthenticatedUser();
        return orderRepository.findByUserWithItemsOrderByCreatedAtDesc(user)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // ── GET /orders/:id — single order
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId) {
        Users user = getAuthenticatedUser();
        Orders order = orderRepository.findByIdAndUser(orderId, user)
                .orElseThrow(() -> new NotFoundException(
                        "Order not found or does not belong to you"));
        return toResponse(order);
    }

    // ── SMS helper
    private void sendOrderConfirmationSms(Users user, Orders order, List<OrderItems> items) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("Hi ").append(user.getName()).append("! Order #")
                    .append(order.getId()).append(" confirmed.\n");
            for (OrderItems item : items) {
                sb.append("- ").append(item.getProduct().getName())
                        .append(" x").append(item.getQuantity())
                        .append(" @ KES ").append(item.getUnitPrice()).append("\n");
            }
            sb.append("Total: KES ").append(order.getTotalAmount());
            smsService.sendSms(user.getMobile(), sb.toString());
        } catch (Exception e) {
            log.error("Failed to send order confirmation SMS for order {}: {}",
                    order.getId(), e.getMessage());
        }
    }
}