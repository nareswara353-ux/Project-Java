package com.example.enterprise.infrastructure.adapter;

import com.example.enterprise.application.port.AuditLogPort;
import com.example.enterprise.domain.Product;
import com.example.enterprise.infrastructure.adapter.entity.AuditLogEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuditLogAdapter implements AuditLogPort {

    private final AuditLogJpaRepository auditLogJpaRepository;

    @Override
    public void logCreated(Product product, String user) {
        saveLog("CREATED", product,
                String.format("Product '%s' created with price=%.2f, stock=%d",
                        product.name(), product.price(), product.stock()),
                user);
    }

    @Override
    public void logUpdated(Product product, Product oldProduct, String user) {
        saveLog("UPDATED", product,
                String.format("Product updated from [name=%s, price=%.2f, stock=%d] to [name=%s, price=%.2f, stock=%d]",
                        oldProduct.name(), oldProduct.price(), oldProduct.stock(),
                        product.name(), product.price(), product.stock()),
                user);
    }

    @Override
    public void logDeleted(Product product, String user) {
        saveLog("DELETED", product,
                String.format("Product '%s' deleted (price=%.2f, stock=%d)",
                        product.name(), product.price(), product.stock()),
                user);
    }

    @Override
    public void logStockAdjusted(Product product, int oldStock, int newStock, String user) {
        saveLog("STOCK_ADJUSTED", product,
                String.format("Stock adjusted from %d to %d (delta=%d)",
                        oldStock, newStock, newStock - oldStock),
                user);
    }

    private void saveLog(String action, Product product, String details, String user) {
        AuditLogEntity entity = AuditLogEntity.builder()
                .action(action)
                .productId(product.id())
                .productName(product.name())
                .details(details)
                .user(user != null ? user : "system")
                .build();
        auditLogJpaRepository.save(entity);
        log.debug("Audit log saved: {} for product {}", action, product.id());
    }
}
