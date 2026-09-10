package com.example.enterprise.infrastructure.adapter.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "audit_log")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String action; // CREATED, UPDATED, DELETED, STOCK_ADJUSTED

    @Column(nullable = false)
    private UUID productId;

    @Column(nullable = false)
    private String productName;

    @Column(columnDefinition = "TEXT")
    private String details;

    @Column(name = "actor", nullable = false)
    private String actor; // rename dari 'user' karena reserved keyword H2

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant timestamp;
}
