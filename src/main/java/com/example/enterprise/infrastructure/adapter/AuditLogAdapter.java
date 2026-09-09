package com.example.enterprise.infrastructure.adapter;

import com.example.enterprise.application.port.AuditLogPort;
import com.example.enterprise.domain.Product;
import com.example.enterprise.infrastructure.adapter.entity.AuditLogEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogAdapter implements AuditLogPort {

    private final JpaAuditLogRepository auditLogRepository;

    @Override
    @Async
    public void logCreated(Product product, String user) {
        saveLog("CREATED", product, null, user, null);
    }

    @Override
    @Async
    public void logUpdated(Product product, Product oldProduct, String user) {
        String details = String.format("Old: price=%.2f, stock=%d | New: price=%.2f, stock=%d",
                oldProduct.price(), oldProduct.stock(),
                product.price(), product.stock());
        saveLog("UPDATED", product, user, details);
    }

    @Override
    @Async
    public void logDeleted(Product product, String user) {
        saveLog("DELETED", product, user, null);
    }

    @Override
    @Async
    public void logStockAdjusted(Product product, int oldStock, int newStock, String user) {
        String details = String.format("Old stock: %d, New stock: %d, Delta: %d",
                oldStock, newStock, newStock - oldStock);
        saveLog("STOCK_ADJUSTED", product, user, details);
    }

    private void saveLog(String action, Product product, String user, String details) {
        AuditLogEntity entity = AuditLogEntity.builder()
                .action(action)
                .productId(product.id())
                .productName(product.name())
                .user(user != null ? user : "system")
                .details(details)
                .build();
        auditLogRepository.save(entity);
        log.debug("Audit log saved: {} - {}", action, product.name());
    }

    private void saveLog(String action, Product product, String user, String details) {
        saveLog(action, product, user, details);
    }
}
