package com.example.enterprise.domain;

import java.util.UUID;

public record Product(
    UUID id,
    String name,
    double price,
    int stock
) {
    public Product {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Product name cannot be null or blank");
        }
        if (price < 0) {
            throw new IllegalArgumentException("Product price cannot be negative");
        }
        if (stock < 0) {
            throw new IllegalArgumentException("Product stock cannot be negative");
        }
    }

    public Product withStock(int newStock) {
        return new Product(this.id, this.name, this.price, newStock);
    }

    public boolean isAvailable() {
        return stock > 0;
    }
}
