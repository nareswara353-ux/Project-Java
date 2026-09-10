package com.example.enterprise.interfaces.rest;

import com.example.enterprise.infrastructure.adapter.AuditLogJpaRepository;
import com.example.enterprise.infrastructure.adapter.entity.AuditLogEntity;
import com.example.enterprise.interfaces.rest.dto.AuditLogResponse;
import com.example.enterprise.interfaces.rest.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    private final AuditLogJpaRepository auditLogJpaRepository;

    @GetMapping
    public ResponseEntity<PageResponse<AuditLogResponse>> getAllAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "timestamp") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction
    ) {
        Pageable pageable = buildPageable(page, size, sortBy, direction);
        Page<AuditLogEntity> auditPage = auditLogJpaRepository.findAll(pageable);
        PageResponse<AuditLogResponse> response = PageResponse.from(auditPage, AuditLogResponse::fromEntity);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<PageResponse<AuditLogResponse>> getAuditLogsByProductId(
            @PathVariable UUID productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "timestamp") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction
    ) {
        Pageable pageable = buildPageable(page, size, sortBy, direction);
        Page<AuditLogEntity> auditPage = auditLogJpaRepository.findByProductId(productId, pageable);
        PageResponse<AuditLogResponse> response = PageResponse.from(auditPage, AuditLogResponse::fromEntity);
        return ResponseEntity.ok(response);
    }

    private Pageable buildPageable(int page, int size, String sortBy, String direction) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        Sort.Direction sortDirection = "ASC".equalsIgnoreCase(direction)
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        Sort sort = Sort.by(sortDirection, sortBy);
        return PageRequest.of(safePage, safeSize, sort);
    }
}
