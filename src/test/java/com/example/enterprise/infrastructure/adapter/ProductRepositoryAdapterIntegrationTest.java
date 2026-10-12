package com.example.enterprise.infrastructure.adapter;

import com.example.enterprise.domain.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(ProductRepositoryAdapter.class)
class ProductRepositoryAdapterIntegrationTest {

    @Autowired
    private ProductRepositoryAdapter repository;

    @Test
    void save_ShouldPersistProduct() {
        Product product = new Product(UUID.randomUUID(), "Laptop", 999.99, 5);

        Product saved = repository.save(product);

        assertThat(saved.id()).isEqualTo(product.id());
        assertThat(saved.name()).isEqualTo("Laptop");
        assertThat(repository.existsById(product.id())).isTrue();
    }

    @Test
    void findById_WhenExists_ShouldReturnProduct() {
        Product product = repository.save(new Product(UUID.randomUUID(), "Monitor", 199.99, 10));

        Optional<Product> found = repository.findById(product.id());

        assertThat(found).isPresent();
        assertThat(found.get().name()).isEqualTo("Monitor");
    }

    @Test
    void findById_WhenNotExists_ShouldReturnEmpty() {
        Optional<Product> found = repository.findById(UUID.randomUUID());

        assertThat(found).isEmpty();
    }

    @Test
    void findAll_ShouldReturnAllPersistedProducts() {
        repository.save(new Product(UUID.randomUUID(), "A", 10.0, 1));
        repository.save(new Product(UUID.randomUUID(), "B", 20.0, 2));

        List<Product> all = repository.findAll();

        assertThat(all).hasSize(2);
        assertThat(all).extracting(Product::name).containsExactlyInAnyOrder("A", "B");
    }

    @Test
    void findByNameContaining_ShouldMatchCaseInsensitive() {
        repository.save(new Product(UUID.randomUUID(), "Gaming Laptop", 1500.0, 3));
        repository.save(new Product(UUID.randomUUID(), "Office Chair", 300.0, 5));

        List<Product> result = repository.findByNameContaining("laptop");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Gaming Laptop");
    }

    @Test
    void findByNameContaining_WithBlank_ShouldReturnAll() {
        repository.save(new Product(UUID.randomUUID(), "A", 10.0, 1));
        repository.save(new Product(UUID.randomUUID(), "B", 20.0, 2));

        List<Product> result = repository.findByNameContaining("  ");

        assertThat(result).hasSize(2);
    }

    @Test
    void deleteById_ShouldRemoveProduct() {
        Product product = repository.save(new Product(UUID.randomUUID(), "Temp", 1.0, 1));

        repository.deleteById(product.id());

        assertThat(repository.existsById(product.id())).isFalse();
    }

    @Test
    void existsById_WhenMissing_ShouldReturnFalse() {
        assertThat(repository.existsById(UUID.randomUUID())).isFalse();
    }
}
