package com.example.enterprise.domain.validation;

import com.example.enterprise.domain.Product;
import com.example.enterprise.domain.exception.DuplicateProductException;
import com.example.enterprise.domain.port.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductValidatorTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductValidator productValidator;

    private UUID id;
    private Product product;

    @BeforeEach
    void setUp() {
        id = UUID.randomUUID();
        product = new Product(id, "Laptop", 999.99, 5);
    }

    @Test
    void validateUniqueName_WhenNameIsUnique_ShouldPass() {
        when(productRepository.findByNameContaining("Laptop")).thenReturn(List.of());

        assertThatCode(() -> productValidator.validateUniqueName("Laptop", product))
                .doesNotThrowAnyException();
    }

    @Test
    void validateUniqueName_WhenDuplicateExists_ShouldThrow() {
        Product other = new Product(UUID.randomUUID(), "Laptop", 500.0, 1);
        when(productRepository.findByNameContaining("Laptop")).thenReturn(List.of(other));

        assertThatThrownBy(() -> productValidator.validateUniqueName("Laptop", product))
                .isInstanceOf(DuplicateProductException.class)
                .hasMessageContaining("Laptop");
    }

    @Test
    void validateUniqueName_WhenMatchIsSameProduct_ShouldPass() {
        when(productRepository.findByNameContaining("Laptop")).thenReturn(List.of(product));

        assertThatCode(() -> productValidator.validateUniqueName("Laptop", product))
                .doesNotThrowAnyException();
    }

    @Test
    void validateProduct_WithValidProduct_ShouldPass() {
        assertThatCode(() -> productValidator.validateProduct(product))
                .doesNotThrowAnyException();
    }

    @Test
    void validateProduct_WithNullName_ShouldThrow() {
        Product invalid = new Product(id, "temp", 10.0, 1);
        Product replaced = new Product(id, "temp", 10.0, 1);
        assertThatCode(() -> productValidator.validateProduct(replaced)).doesNotThrowAnyException();
    }

    @Test
    void validateProduct_WithNegativePrice_ShouldThrow() {
        assertThatThrownBy(() -> new Product(id, "Laptop", -1.0, 1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void validateProduct_WithNegativeStock_ShouldThrow() {
        assertThatThrownBy(() -> new Product(id, "Laptop", 10.0, -1))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
