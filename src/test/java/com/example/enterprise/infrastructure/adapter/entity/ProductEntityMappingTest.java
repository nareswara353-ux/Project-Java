package com.example.enterprise.infrastructure.adapter.entity;

import com.example.enterprise.domain.Product;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ProductEntityMappingTest {

    @Test
    void fromDomain_ShouldCopyAllFields() {
        UUID id = UUID.randomUUID();
        Product product = new Product(id, "Laptop", 999.99, 5);

        ProductEntity entity = ProductEntity.fromDomain(product);

        assertThat(entity.getId()).isEqualTo(id);
        assertThat(entity.getName()).isEqualTo("Laptop");
        assertThat(entity.getPrice()).isEqualTo(999.99);
        assertThat(entity.getStock()).isEqualTo(5);
        assertThat(entity.getVersion()).isEqualTo(0L);
    }

    @Test
    void toDomain_ShouldCopyAllFields() {
        UUID id = UUID.randomUUID();
        ProductEntity entity = ProductEntity.builder()
                .id(id)
                .name("Monitor")
                .price(199.99)
                .stock(10)
                .build();

        Product product = entity.toDomain();

        assertThat(product.id()).isEqualTo(id);
        assertThat(product.name()).isEqualTo("Monitor");
        assertThat(product.price()).isEqualTo(199.99);
        assertThat(product.stock()).isEqualTo(10);
    }

    @Test
    void roundtrip_ShouldPreserveData() {
        Product original = new Product(UUID.randomUUID(), "Keyboard", 49.99, 12);

        Product roundtrip = ProductEntity.fromDomain(original).toDomain();

        assertThat(roundtrip).isEqualTo(original);
    }
}
