package com.example.enterprise.infrastructure.listener;

import com.example.enterprise.application.port.AuditLogPort;
import com.example.enterprise.domain.Product;
import com.example.enterprise.domain.event.ProductEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class ProductEventListenerTest {

    @Mock
    private AuditLogPort auditLogPort;

    @InjectMocks
    private ProductEventListener listener;

    private Product product;
    private UUID productId;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        product = new Product(productId, "Laptop", 999.99, 5);
    }

    @Test
    void handleProductCreated_ShouldDelegateToAuditLogCreated() {
        ProductEvent event = new ProductEvent.ProductCreated(product);

        listener.handleProductEvent(event);

        verify(auditLogPort, times(1)).logCreated(eq(product), eq("system"));
        verifyNoMoreInteractions(auditLogPort);
    }

    @Test
    void handleProductUpdated_ShouldDelegateToAuditLogUpdated() {
        Product oldProduct = new Product(productId, "Old Laptop", 500.0, 3);
        ProductEvent event = new ProductEvent.ProductUpdated(product, oldProduct);

        listener.handleProductEvent(event);

        verify(auditLogPort, times(1)).logUpdated(eq(product), eq(oldProduct), eq("system"));
        verifyNoMoreInteractions(auditLogPort);
    }

    @Test
    void handleProductDeleted_ShouldDelegateToAuditLogDeleted() {
        ProductEvent event = new ProductEvent.ProductDeleted(productId, product);

        listener.handleProductEvent(event);

        verify(auditLogPort, times(1)).logDeleted(eq(product), eq("system"));
        verifyNoMoreInteractions(auditLogPort);
    }

    @Test
    void handleStockAdjusted_ShouldDelegateToAuditLogStockAdjusted() {
        Product adjusted = new Product(productId, "Laptop", 999.99, 10);
        ProductEvent event = new ProductEvent.ProductStockAdjusted(adjusted, 5, 10);

        listener.handleProductEvent(event);

        verify(auditLogPort, times(1)).logStockAdjusted(eq(adjusted), eq(5), eq(10), eq("system"));
        verifyNoMoreInteractions(auditLogPort);
    }
}
