package com.example.enterprise.infrastructure.scheduler;

import com.example.enterprise.infrastructure.adapter.AuditLogJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuditLogRetentionScheduler {

    private final AuditLogJpaRepository auditLogJpaRepository;

    @Value("${app.audit.retention-days:90}")
    private long retentionDays;

    @Scheduled(cron = "${app.audit.cleanup-cron:0 0 3 * * ?}")
    @Transactional
    public void purgeOldAuditLogs() {
        Instant cutoff = Instant.now().minus(retentionDays, ChronoUnit.DAYS);
        log.info("Starting audit log cleanup: deleting entries older than {} days (cutoff={})",
                retentionDays, cutoff);

        long deleted = auditLogJpaRepository.deleteByTimestampBefore(cutoff);

        log.info("Audit log cleanup finished: {} entries removed", deleted);
    }
}
