package com.example.demo.service;


import com.example.demo.enums.OrderStatus;
import com.example.demo.models.CartItems;
import com.example.demo.models.OrderItems;
import com.example.demo.models.Orders;
import com.example.demo.repository.OrderItemRepository;
import com.example.demo.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartService cartService;
    private final ProductService productService;

    public List<Orders> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    public Orders getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
    }

    public List<OrderItems> getOrderItems(Long orderId) {
        return orderItemRepository.findByOrderId(orderId);
    }

    @Transactional
    public Orders placeOrderFromCart(Long userId) {
        List<CartItems> cartItems = cartService.getCartByUserId(Math.toIntExact(userId));

        if (cartItems.isEmpty()) {
            throw new RuntimeException("Cart is empty for user: " + userId);
        }

        BigDecimal total = cartItems.stream()
                .map(CartItems::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalQuantity = cartItems.stream()
                .mapToInt(CartItems::getQuantity)
                .sum();

        Orders order = Orders.builder()
                .userId(userId)
                .quantity(totalQuantity)
                .totalAmount(total)
                .status(OrderStatus.PENDING)
                .build();

        Orders savedOrder = orderRepository.save(order);

        for (CartItems cartItem : cartItems) {
            var product = productService.getProductById(Long.valueOf(cartItem.getProductId()));

            OrderItems orderItem = OrderItems.builder()
                    .orderId(savedOrder.getId())
                    .productId(Long.valueOf(cartItem.getProductId()))
                    .quantity(cartItem.getQuantity())
                    .unitPrice(product.getPrice())
                    .build();

            orderItemRepository.save(orderItem);
            productService.adjustStock(Long.valueOf(cartItem.getProductId()), -cartItem.getQuantity());
        }

        cartService.clearCart(Math.toIntExact(userId));

        return savedOrder;
    }

    public Orders updateOrderStatus(Long orderId, OrderStatus status) {
        Orders order = getOrderById(orderId);
        order.setStatus(status);
        return orderRepository.save(order);
    }

    public void cancelOrder(Long orderId) {
        Orders order = getOrderById(orderId);
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new RuntimeException("Only PENDING orders can be cancelled");
        }

        // Restore stock
        List<OrderItems> items = orderItemRepository.findByOrderId(orderId);
        for (OrderItems item : items) {
            productService.adjustStock(item.getProductId(), item.getQuantity());
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }
}
