package com.example.demo.repository;


import com.example.demo.models.Orders;
import com.example.demo.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Orders, Long> {
    List<Orders> findByUserId(Long userId);
    List<Orders> findByStatus(OrderStatus status);
    List<Orders> findByUserIdAndStatus(Long userId, OrderStatus status);
}
