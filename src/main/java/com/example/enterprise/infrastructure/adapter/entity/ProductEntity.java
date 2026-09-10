package com.example.enterprise.infrastructure.adapter.entity;

import com.example.enterprise.domain.Product;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "products")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Version
    @Column(nullable = false)
    @Builder.Default
    private Long version = 0L;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(nullable = false)
    private double price;

    @Column(nullable = false)
    private int stock;

    public static ProductEntity fromDomain(Product product) {
        return ProductEntity.builder()
                .id(product.id())
                .name(product.name())
                .price(product.price())
                .stock(product.stock())
                .build();
    }

    public Product toDomain() {
        return new Product(
                this.id,
                this.name,
                this.price,
                this.stock
        );
    }
}
