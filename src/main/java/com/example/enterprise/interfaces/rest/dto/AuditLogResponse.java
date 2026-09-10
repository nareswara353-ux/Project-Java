package com.example.enterprise.interfaces.rest.dto;

import com.example.enterprise.infrastructure.adapter.entity.AuditLogEntity;

import java.time.Instant;
import java.util.UUID;

public record AuditLogResponse(
        UUID id,
        String action,
        UUID productId,
        String productName,
        String details,
        String user,
        Instant timestamp
) {
    public static AuditLogResponse fromEntity(AuditLogEntity entity) {
        return new AuditLogResponse(
                entity.getId(),
                entity.getAction(),
                entity.getProductId(),
                entity.getProductName(),
                entity.getDetails(),
                entity.getUser(),
                entity.getTimestamp()
        );
    }
}
