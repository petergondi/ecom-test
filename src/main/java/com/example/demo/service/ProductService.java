// ProductService.java
package com.example.demo.service;

import com.example.demo.exception.NotFoundException;
import com.example.demo.models.Products;
import com.example.demo.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public Map<String, Object> getAllProducts(int page, int limit) {
        // Enforce max limit of 50
        limit = Math.min(limit, 50);
        // Convert to 0-based page index
        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<Products> result = productRepository.findAll(pageable);

        return Map.of(
                "data", result.getContent(),
                "meta", Map.of(
                        "total", result.getTotalElements(),
                        "page", page,
                        "limit", limit,
                        "totalPages", result.getTotalPages()
                )
        );
    }

    public Products getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found with id: " + id));
    }

    public List<Products> getProductsByCategory(String category) {
        return productRepository.findByCategory(category);
    }

    public List<Products> getInStockProducts() {
        return productRepository.findByStockGreaterThan(0);
    }

    public Products createProduct(Products product) {
        return productRepository.save(product);
    }

    public Products updateProduct(Long id, Products updatedProduct) {
        Products existing = getProductById(id);
        existing.setName(updatedProduct.getName());
        existing.setDescription(updatedProduct.getDescription());
        existing.setPrice(updatedProduct.getPrice());
        existing.setCategory(updatedProduct.getCategory());
        existing.setStock(updatedProduct.getStock());
        return productRepository.save(existing);
    }

    public Products adjustStock(Long id, int delta) {
        Products product = getProductById(id);
        int newStock = product.getStock() + delta;
        if (newStock < 0) {
            throw new RuntimeException("Insufficient stock for product id: " + id);
        }
        product.setStock(newStock);
        return productRepository.save(product);
    }

    public void deleteProduct(Long id) {
        getProductById(id);
        productRepository.deleteById(id);
    }
}