package com.example.enterprise.interfaces.rest.dto;

import com.example.enterprise.domain.Product;

import java.util.UUID;

public record ProductResponse(
        UUID id,
        String name,
        double price,
        int stock,
        boolean available
) {
    public static ProductResponse fromDomain(Product product) {
        return new ProductResponse(
                product.id(),
                product.name(),
                product.price(),
                product.stock(),
                product.isAvailable()
        );
    }
}
