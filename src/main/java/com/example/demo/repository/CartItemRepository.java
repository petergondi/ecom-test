package com.example.demo.repository;


import com.example.demo.models.CartItems;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItems, Integer> {
    List<CartItems> findByUserId(Integer userId);
    Optional<CartItems> findByUserIdAndProductId(Integer userId, Integer productId);
    void deleteByUserId(Integer userId);
}
