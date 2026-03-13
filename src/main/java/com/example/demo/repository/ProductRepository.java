package com.example.demo.repository;

import com.example.demo.models.Products;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Products, Long> {
    List<Products> findByCategory(String category);
    List<Products> findByStockGreaterThan(int stock);
}