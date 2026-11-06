package com.example.enterprise.infrastructure.listener;

import com.example.enterprise.application.port.AuditLogPort;
import com.example.enterprise.domain.event.ProductEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductEventListener {

    private final AuditLogPort auditLogPort;

    @Async
    @EventListener
    public void handleProductEvent(ProductEvent event) {
        log.info("Received product event: {} on thread {}",
                event.getClass().getSimpleName(), Thread.currentThread());

        switch (event) {
            case ProductEvent.ProductCreated created -> {
                log.info("Product created: {} (ID: {})",
                        created.productSnapshot().name(),
                        created.productId());
                auditLogPort.logCreated(created.productSnapshot(), "system");
            }

            case ProductEvent.ProductUpdated updated -> {
                log.info("Product updated: {} (ID: {})",
                        updated.productSnapshot().name(),
                        updated.productId());
                auditLogPort.logUpdated(updated.productSnapshot(), updated.oldProduct(), "system");
            }

            case ProductEvent.ProductDeleted deleted -> {
                log.info("Product deleted: {} (ID: {})",
                        deleted.product().name(),
                        deleted.productId());
                auditLogPort.logDeleted(deleted.productSnapshot(), "system");
            }

            case ProductEvent.ProductStockAdjusted adjusted -> {
                log.info("Product stock adjusted: {} (ID: {}) | {} -> {}",
                        adjusted.productSnapshot().name(),
                        adjusted.productId(),
                        adjusted.oldStock(),
                        adjusted.newStock());
                auditLogPort.logStockAdjusted(
                        adjusted.productSnapshot(),
                        adjusted.oldStock(),
                        adjusted.newStock(),
                        "system"
                );
            }
        }
    }
}
