package com.example.demo.service;


import com.example.demo.models.Products;
import com.example.demo.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public List<Products> getAllProducts() {
        return productRepository.findAll();
    }

    public Products getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
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
        getProductById(id); // ensure exists
        productRepository.deleteById(id);
    }
}
