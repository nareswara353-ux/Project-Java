package com.example.enterprise.interfaces.rest;

import com.example.enterprise.infrastructure.adapter.AuditLogJpaRepository;
import com.example.enterprise.interfaces.rest.dto.AuditLogResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogJpaRepository auditLogJpaRepository;

    @GetMapping
    public ResponseEntity<List<AuditLogResponse>> getAllAuditLogs() {
        List<AuditLogResponse> responses = auditLogJpaRepository
                .findAll(Sort.by(Sort.Direction.DESC, "timestamp"))
                .stream()
                .map(AuditLogResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<AuditLogResponse>> getAuditLogsByProductId(@PathVariable UUID productId) {
        List<AuditLogResponse> responses = auditLogJpaRepository
                .findAll(Sort.by(Sort.Direction.DESC, "timestamp"))
                .stream()
                .filter(log -> log.getProductId().equals(productId))
                .map(AuditLogResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }
}
