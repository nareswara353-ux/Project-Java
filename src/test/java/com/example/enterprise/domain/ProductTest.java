package com.example.enterprise.domain;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductTest {

    private static final UUID ID = UUID.randomUUID();

    @Test
    void constructor_WithValidData_ShouldCreateProduct() {
        Product product = new Product(ID, "Laptop", 999.99, 5);

        assertThat(product.id()).isEqualTo(ID);
        assertThat(product.name()).isEqualTo("Laptop");
        assertThat(product.price()).isEqualTo(999.99);
        assertThat(product.stock()).isEqualTo(5);
    }

    @Test
    void constructor_WithNullName_ShouldThrow() {
        assertThatThrownBy(() -> new Product(ID, null, 10.0, 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("name");
    }

    @Test
    void constructor_WithBlankName_ShouldThrow() {
        assertThatThrownBy(() -> new Product(ID, "   ", 10.0, 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("name");
    }

    @Test
    void constructor_WithNegativePrice_ShouldThrow() {
        assertThatThrownBy(() -> new Product(ID, "Laptop", -1.0, 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("price");
    }

    @Test
    void constructor_WithNegativeStock_ShouldThrow() {
        assertThatThrownBy(() -> new Product(ID, "Laptop", 10.0, -1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("stock");
    }

    @Test
    void withStock_ShouldReturnNewProductWithUpdatedStock() {
        Product original = new Product(ID, "Laptop", 999.99, 5);

        Product updated = original.withStock(10);

        assertThat(updated.stock()).isEqualTo(10);
        assertThat(updated.id()).isEqualTo(original.id());
        assertThat(updated.name()).isEqualTo(original.name());
        assertThat(updated.price()).isEqualTo(original.price());
        assertThat(original.stock()).isEqualTo(5);
    }

    @Test
    void isAvailable_WhenStockPositive_ShouldReturnTrue() {
        Product product = new Product(ID, "Laptop", 10.0, 1);

        assertThat(product.isAvailable()).isTrue();
    }

    @Test
    void isAvailable_WhenStockZero_ShouldReturnFalse() {
        Product product = new Product(ID, "Laptop", 10.0, 0);

        assertThat(product.isAvailable()).isFalse();
    }

    @Test
    void equalsAndHashCode_ShouldBeValueBased() {
        Product a = new Product(ID, "Laptop", 10.0, 1);
        Product b = new Product(ID, "Laptop", 10.0, 1);

        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }
}
