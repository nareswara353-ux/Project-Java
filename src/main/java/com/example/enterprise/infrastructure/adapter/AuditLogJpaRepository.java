package com.example.enterprise.infrastructure.adapter;

import com.example.enterprise.infrastructure.adapter.entity.AuditLogEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AuditLogJpaRepository extends JpaRepository<AuditLogEntity, UUID> {

    Page<AuditLogEntity> findByProductId(UUID productId, Pageable pageable);
}
