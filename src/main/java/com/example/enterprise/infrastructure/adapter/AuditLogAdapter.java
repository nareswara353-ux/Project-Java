package com.example.enterprise.infrastructure.adapter;

import com.example.enterprise.application.port.AuditLogPort;
import com.example.enterprise.domain.Product;
import com.example.enterprise.infrastructure.adapter.entity.AuditLogEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuditLogAdapter implements AuditLogPort {

    private final AuditLogRepository auditLogRepository;

    @Override
    @Transactional
    public void logCreated(Product product, String user) {
        saveLog("CREATED", product, null, user, null);
    }

    @Override
    @Transactional
    public void logUpdated(Product product, Product oldProduct, String user) {
        String details = String.format("Old: price=%.2f, stock=%d | New: price=%.2f, stock=%d",
                oldProduct.price(), oldProduct.stock(),
                product.price(), product.stock());
        saveLog("UPDATED", product, user, details, null);
    }

    @Override
    @Transactional
    public void logDeleted(Product product, String user) {
        saveLog("DELETED", product, user, "Product deleted", null);
    }

    @Override
    @Transactional
    public void logStockAdjusted(Product product, int oldStock, int newStock, String user) {
        String details = String.format("Stock adjusted: %d → %d (delta: %d)",
                oldStock, newStock, newStock - oldStock);
        saveLog("STOCK_ADJUSTED", product, user, details, null);
    }

    private void saveLog(String action, Product product, String user, String details, String unused) {
        AuditLogEntity entity = AuditLogEntity.builder()
                .action(action)
                .productId(product.id())
                .productName(product.name())
                .user(user != null ? user : "system")
                .details(details != null ? details : "")
                .build();
        auditLogRepository.save(entity);
        log.debug("Audit log saved: {} for product {}", action, product.id());
    }
}
