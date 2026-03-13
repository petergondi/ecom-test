package com.example.demo.repository;

import com.example.demo.models.CartItems;
import com.example.demo.models.Products;
import com.example.demo.models.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItems, Integer> {
    List<CartItems> findByUser(Users user);
    Optional<CartItems> findByUserAndProduct(Users user, Products product);
    Optional<CartItems> findByIdAndUser(Integer id, Users user);
    void deleteByUser(Users user);
}