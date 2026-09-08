package com.example.enterprise.application.service;

import com.example.enterprise.domain.Product;
import com.example.enterprise.domain.port.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductMetricsService {

    private final ProductRepository productRepository;

    public long getTotalProducts() {
        return productRepository.findAll().size();
    }

    public double getAveragePrice() {
        List<Product> products = productRepository.findAll();
        if (products.isEmpty()) {
            return 0.0;
        }
        return products.stream()
                .mapToDouble(Product::price)
                .average()
                .orElse(0.0);
    }

    public int getTotalStock() {
        return productRepository.findAll().stream()
                .mapToInt(Product::stock)
                .sum();
    }

    public List<Product> getLowStockProducts(int threshold) {
        return productRepository.findAll().stream()
                .filter(p -> p.stock() < threshold)
                .toList();
    }
}
