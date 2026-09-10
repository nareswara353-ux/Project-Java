package com.example.enterprise.application.service;

import com.example.enterprise.application.port.EventPublisher;
import com.example.enterprise.domain.Product;
import com.example.enterprise.domain.exception.ProductNotFoundException;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private ProductServiceImpl productService;

    private UUID productId;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        testProduct = new Product(productId, "Test Product", 99.99, 10);
    }

    @Test
    void createProduct_ShouldSaveAndReturnProduct() {
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);
        doNothing().when(eventPublisher).publish(any());

        Product result = productService.createProduct("Test Product", 99.99, 10);

        assertNotNull(result);
        assertEquals(testProduct, result);
        verify(productRepository, times(1)).save(any(Product.class));
        verify(eventPublisher, times(1)).publish(any());
    }

    @Test
    void updateProduct_WhenExists_ShouldUpdateAndReturn() {
        Product updated = new Product(productId, "Updated Product", 49.99, 5);
        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(updated);
        doNothing().when(eventPublisher).publish(any());

        Product result = productService.updateProduct(productId, "Updated Product", 49.99, 5);

        assertNotNull(result);
        assertEquals("Updated Product", result.name());
        assertEquals(49.99, result.price());
        assertEquals(5, result.stock());
        verify(productRepository, times(1)).findById(productId);
        verify(productRepository, times(1)).save(any(Product.class));
        verify(eventPublisher, times(1)).publish(any());
    }

    @Test
    void updateProduct_WhenNotFound_ShouldThrowProductNotFoundException() {
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class,
                () -> productService.updateProduct(productId, "Any", 10.0, 1));
        verify(productRepository, never()).save(any(Product.class));
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    void getProductById_WhenExists_ShouldReturnProduct() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));

        Product result = productService.getProductById(productId);

        assertNotNull(result);
        assertEquals(testProduct, result);
    }

    @Test
    void getProductById_WhenNotFound_ShouldThrowProductNotFoundException() {
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class,
                () -> productService.getProductById(productId));
    }

    @Test
    void getAllProducts_ShouldReturnList() {
        when(productRepository.findAll()).thenReturn(List.of(testProduct));

        List<Product> result = productService.getAllProducts();

        assertEquals(1, result.size());
        assertEquals(testProduct, result.get(0));
    }

    @Test
    void deleteProduct_WhenExists_ShouldDelete() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));
        doNothing().when(productRepository).deleteById(productId);
        doNothing().when(eventPublisher).publish(any());

        assertDoesNotThrow(() -> productService.deleteProduct(productId));
        verify(productRepository, times(1)).deleteById(productId);
        verify(eventPublisher, times(1)).publish(any());
    }

    @Test
    void deleteProduct_WhenNotFound_ShouldThrowProductNotFoundException() {
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class,
                () -> productService.deleteProduct(productId));
        verify(productRepository, never()).deleteById(any());
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    void adjustStock_WhenPositiveDelta_ShouldIncreaseStock() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));
        Product adjusted = new Product(productId, "Test Product", 99.99, 15);
        when(productRepository.save(any(Product.class))).thenReturn(adjusted);
        doNothing().when(eventPublisher).publish(any());

        Product result = productService.adjustStock(productId, 5);

        assertEquals(15, result.stock());
        verify(productRepository, times(1)).save(any(Product.class));
        verify(eventPublisher, times(1)).publish(any());
    }

    @Test
    void adjustStock_WhenDeltaCausesNegativeStock_ShouldThrowException() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));

        assertThrows(IllegalArgumentException.class,
                () -> productService.adjustStock(productId, -20));
        verify(productRepository, never()).save(any(Product.class));
        verify(eventPublisher, never()).publish(any());
    }
}
