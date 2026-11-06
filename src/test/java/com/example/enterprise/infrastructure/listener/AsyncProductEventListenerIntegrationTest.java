package com.example.enterprise.infrastructure.listener;

import com.example.enterprise.application.port.AuditLogPort;
import com.example.enterprise.domain.Product;
import com.example.enterprise.domain.event.ProductEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Duration;
import java.util.UUID;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@SpringBootTest
class AsyncProductEventListenerIntegrationTest {

    @Autowired
    private ProductEventListener listener;

    @MockitoBean
    private AuditLogPort auditLogPort;

    private Product sampleProduct() {
        return new Product(UUID.randomUUID(), "Async-Test", 100.0, 5);
    }

    @Test
    void handleProductEvent_Created_ShouldInvokeAuditLogAsynchronously() {
        Product product = sampleProduct();
        ProductEvent event = new ProductEvent.ProductCreated(product);

        listener.handleProductEvent(event);

        await().atMost(Duration.ofSeconds(3))
                .untilAsserted(() -> verify(auditLogPort).logCreated(eq(product), eq("system")));
    }

    @Test
    void handleProductEvent_StockAdjusted_ShouldInvokeAuditLogAsynchronously() {
        Product product = sampleProduct();
        Product adjusted = product.withStock(10);
        ProductEvent event = new ProductEvent.ProductStockAdjusted(adjusted, 5, 10);

        listener.handleProductEvent(event);

        await().atMost(Duration.ofSeconds(3))
                .untilAsserted(() -> verify(auditLogPort)
                        .logStockAdjusted(eq(adjusted), eq(5), eq(10), eq("system")));
    }

    @Test
    void handleProductEvent_Deleted_ShouldInvokeAuditLogAsynchronously() {
        Product product = sampleProduct();
        ProductEvent event = new ProductEvent.ProductDeleted(product.id(), product);

        listener.handleProductEvent(event);

        await().atMost(Duration.ofSeconds(3))
                .untilAsserted(() -> verify(auditLogPort).logDeleted(eq(product), eq("system")));
    }
}
