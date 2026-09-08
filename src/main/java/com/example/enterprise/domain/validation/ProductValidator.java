package com.example.enterprise.domain.validation;

import com.example.enterprise.domain.Product;
import com.example.enterprise.domain.exception.DuplicateProductException;
import com.example.enterprise.domain.port.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductValidator {

    private final ProductRepository productRepository;

    public void validateUniqueName(String name, Product product) {
        productRepository.findByNameContaining(name).stream()
                .filter(p -> p.name().equalsIgnoreCase(name))
                .filter(p -> !p.id().equals(product.id()))
                .findFirst()
                .ifPresent(p -> {
                    throw new DuplicateProductException(name);
                });
    }

    public void validateProduct(Product product) {
        if (product.name() == null || product.name().isBlank()) {
            throw new IllegalArgumentException("Product name cannot be null or blank");
        }
        if (product.price() < 0) {
            throw new IllegalArgumentException("Product price cannot be negative");
        }
        if (product.stock() < 0) {
            throw new IllegalArgumentException("Product stock cannot be negative");
        }
        // Additional domain rules can be added here
    }
}
