package com.example.enterprise.application.service;

import com.example.enterprise.domain.Product;
import com.example.enterprise.domain.port.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private UUID productId;
    private Product product;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        product = new Product(productId, "Test Product", 29.99, 100);
    }

    @Test
    void shouldCreateProduct() {
        when(productRepository.save(any(Product.class))).thenReturn(product);

        Product created = productService.createProduct("Test Product", 29.99, 100);

        assertThat(created).isNotNull();
        assertThat(created.name()).isEqualTo("Test Product");
        assertThat(created.price()).isEqualTo(29.99);
        assertThat(created.stock()).isEqualTo(100);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void shouldGetProductById() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        Product found = productService.getProductById(productId);

        assertThat(found).isEqualTo(product);
        verify(productRepository).findById(productId);
    }

    @Test
    void shouldThrowWhenProductNotFound() {
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById(productId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Product not found with id: " + productId);
    }

    @Test
    void shouldUpdateProduct() {
        Product updatedProduct = new Product(productId, "Updated Name", 39.99, 50);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);

        Product result = productService.updateProduct(productId, "Updated Name", 39.99, 50);

        assertThat(result.name()).isEqualTo("Updated Name");
        assertThat(result.price()).isEqualTo(39.99);
        assertThat(result.stock()).isEqualTo(50);
        verify(productRepository).findById(productId);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void shouldGetAllProducts() {
        when(productRepository.findAll()).thenReturn(List.of(product));

        List<Product> products = productService.getAllProducts();

        assertThat(products).hasSize(1);
        assertThat(products.get(0)).isEqualTo(product);
        verify(productRepository).findAll();
    }

    @Test
    void shouldDeleteProduct() {
        when(productRepository.existsById(productId)).thenReturn(true);
        doNothing().when(productRepository).deleteById(productId);

        productService.deleteProduct(productId);

        verify(productRepository).existsById(productId);
        verify(productRepository).deleteById(productId);
    }

    @Test
    void shouldThrowWhenDeletingNonExistingProduct() {
        when(productRepository.existsById(productId)).thenReturn(false);

        assertThatThrownBy(() -> productService.deleteProduct(productId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Product not found with id: " + productId);
    }

    @Test
    void shouldAdjustStock() {
        Product adjustedProduct = new Product(productId, "Test Product", 29.99, 120);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(adjustedProduct);

        Product result = productService.adjustStock(productId, 20);

        assertThat(result.stock()).isEqualTo(120);
        verify(productRepository).findById(productId);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void shouldThrowWhenAdjustingStockToNegative() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> productService.adjustStock(productId, -150))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Stock cannot be negative after adjustment");
    }
}
