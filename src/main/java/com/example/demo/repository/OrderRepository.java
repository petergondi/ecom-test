package com.example.demo.repository;

import com.example.demo.models.Orders;
import com.example.demo.models.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Orders, Long> {

    // Order history — newest first, eager load items + products
    @Query("SELECT DISTINCT o FROM Orders o " +
            "LEFT JOIN FETCH o.items i " +
            "LEFT JOIN FETCH i.product " +
            "WHERE o.user = :user " +
            "ORDER BY o.createdAt DESC")
    List<Orders> findByUserWithItemsOrderByCreatedAtDesc(@Param("user") Users user);

    // Single order scoped to user
    @Query("SELECT o FROM Orders o " +
            "LEFT JOIN FETCH o.items i " +
            "LEFT JOIN FETCH i.product " +
            "WHERE o.id = :id AND o.user = :user")
    Optional<Orders> findByIdAndUser(@Param("id") Long id, @Param("user") Users user);

    // Idempotency check — same user, same key, within last 10 seconds
    @Query("SELECT o FROM Orders o " +
            "LEFT JOIN FETCH o.items i " +
            "LEFT JOIN FETCH i.product " +
            "WHERE o.user = :user " +
            "AND o.idempotencyKey = :key " +
            "AND o.createdAt >= :since")
    Optional<Orders> findRecentByUserAndIdempotencyKey(
            @Param("user") Users user,
            @Param("key") String key,
            @Param("since") LocalDateTime since);
}