package com.example.enterprise.application.port;

import com.example.enterprise.domain.Product;

import java.time.Instant;

public interface AuditLogPort {
    void logCreated(Product product, String user);
    void logUpdated(Product product, Product oldProduct, String user);
    void logDeleted(Product product, String user);
    void logStockAdjusted(Product product, int oldStock, int newStock, String user);
}
