package com.example.enterprise.domain.event;

import com.example.enterprise.domain.Product;

import java.time.Instant;
import java.util.UUID;

public sealed interface ProductEvent {

    UUID productId();
    Instant timestamp();
    Product productSnapshot();

    record ProductCreated(Product product, Instant timestamp) implements ProductEvent {
        public ProductCreated(Product product) {
            this(product, Instant.now());
        }

        @Override
        public UUID productId() {
            return product.id();
        }

        @Override
        public Product productSnapshot() {
            return product;
        }
    }

    record ProductUpdated(Product product, Product oldProduct, Instant timestamp) implements ProductEvent {
        public ProductUpdated(Product product, Product oldProduct) {
            this(product, oldProduct, Instant.now());
        }

        @Override
        public UUID productId() {
            return product.id();
        }

        @Override
        public Product productSnapshot() {
            return product;
        }
    }

    record ProductDeleted(UUID productId, Product product, Instant timestamp) implements ProductEvent {
        public ProductDeleted(UUID productId, Product product) {
            this(productId, product, Instant.now());
        }

        @Override
        public Product productSnapshot() {
            return product;
        }
    }

    record ProductStockAdjusted(Product product, int oldStock, int newStock, Instant timestamp) implements ProductEvent {
        public ProductStockAdjusted(Product product, int oldStock, int newStock) {
            this(product, oldStock, newStock, Instant.now());
        }

        @Override
        public UUID productId() {
            return product.id();
        }

        @Override
        public Product productSnapshot() {
            return product;
        }
    }
}
