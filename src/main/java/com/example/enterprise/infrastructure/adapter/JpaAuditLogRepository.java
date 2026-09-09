package com.example.enterprise.infrastructure.adapter;

import com.example.enterprise.infrastructure.adapter.entity.AuditLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface JpaAuditLogRepository extends JpaRepository<AuditLogEntity, UUID> {
}
