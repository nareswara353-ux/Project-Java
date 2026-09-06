package com.example.enterprise.infrastructure.listener;

import com.example.enterprise.domain.event.ProductEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ProductEventListener {

    @EventListener
    public void handleProductEvent(ProductEvent event) {
        log.info("Received product event: {}", event.getClass().getSimpleName());

        // Pattern matching dengan Java 21 preview
        switch (event) {
            case ProductEvent.ProductCreated created ->
                log.info("Product created: {} (ID: {}, Price: {}, Stock: {})",
                        created.productSnapshot().name(),
                        created.productId(),
                        created.productSnapshot().price(),
                        created.productSnapshot().stock());

            case ProductEvent.ProductUpdated updated ->
                log.info("Product updated: {} (ID: {}) | Old: Price={}, Stock={} | New: Price={}, Stock={}",
                        updated.productSnapshot().name(),
                        updated.productId(),
                        updated.oldProduct().price(),
                        updated.oldProduct().stock(),
                        updated.productSnapshot().price(),
                        updated.productSnapshot().stock());

            case ProductEvent.ProductDeleted deleted ->
                log.info("Product deleted: {} (ID: {}) - Deleted product: {}, Price: {}, Stock: {}",
                        deleted.product().name(),
                        deleted.productId(),
                        deleted.productSnapshot().name(),
                        deleted.productSnapshot().price(),
                        deleted.productSnapshot().stock());

            case ProductEvent.ProductStockAdjusted adjusted ->
                log.info("Product stock adjusted: {} (ID: {}) | Old stock: {}, New stock: {}, Delta: {}",
                        adjusted.productSnapshot().name(),
                        adjusted.productId(),
                        adjusted.oldStock(),
                        adjusted.newStock(),
                        adjusted.newStock() - adjusted.oldStock());
        }
    }
}
