package com.example.enterprise.infrastructure.scheduler;

import com.example.enterprise.infrastructure.adapter.AuditLogJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditLogRetentionSchedulerTest {

    @Mock
    private AuditLogJpaRepository auditLogJpaRepository;

    private AuditLogRetentionScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new AuditLogRetentionScheduler(auditLogJpaRepository);
        ReflectionTestUtils.setField(scheduler, "retentionDays", 30L);
    }

    @Test
    void purgeOldAuditLogs_ShouldDeleteEntriesOlderThanRetentionDays() {
        when(auditLogJpaRepository.deleteByTimestampBefore(any(Instant.class))).thenReturn(7L);

        scheduler.purgeOldAuditLogs();

        ArgumentCaptor<Instant> captor = ArgumentCaptor.forClass(Instant.class);
        verify(auditLogJpaRepository).deleteByTimestampBefore(captor.capture());

        Instant cutoff = captor.getValue();
        Instant expected = Instant.now().minus(30, ChronoUnit.DAYS);

        assertThat(cutoff)
                .isAfter(expected.minusSeconds(5))
                .isBefore(expected.plusSeconds(5));
    }

    @Test
    void purgeOldAuditLogs_WhenNothingDeleted_ShouldCompleteWithoutError() {
        when(auditLogJpaRepository.deleteByTimestampBefore(any(Instant.class))).thenReturn(0L);

        scheduler.purgeOldAuditLogs();

        verify(auditLogJpaRepository).deleteByTimestampBefore(any(Instant.class));
    }
}
