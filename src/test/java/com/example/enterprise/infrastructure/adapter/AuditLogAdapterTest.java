package com.example.enterprise.infrastructure.adapter;

import com.example.enterprise.domain.Product;
import com.example.enterprise.infrastructure.adapter.entity.AuditLogEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuditLogAdapterTest {

    @Mock
    private AuditLogJpaRepository auditLogJpaRepository;

    @InjectMocks
    private AuditLogAdapter auditLogAdapter;

    private Product product;
    private UUID productId;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        product = new Product(productId, "Laptop", 999.99, 5);
    }

    @Test
    void logCreated_ShouldSaveAuditLogWithCreatedAction() {
        auditLogAdapter.logCreated(product, "admin");

        ArgumentCaptor<AuditLogEntity> captor = ArgumentCaptor.forClass(AuditLogEntity.class);
        verify(auditLogJpaRepository, times(1)).save(captor.capture());
        AuditLogEntity saved = captor.getValue();

        assertThat(saved.getAction()).isEqualTo("CREATED");
        assertThat(saved.getProductId()).isEqualTo(productId);
        assertThat(saved.getProductName()).isEqualTo("Laptop");
        assertThat(saved.getActor()).isEqualTo("admin");
        assertThat(saved.getDetails()).contains("Laptop").contains("999.99").contains("5");
    }

    @Test
    void logCreated_WithNullActor_ShouldUseSystem() {
        auditLogAdapter.logCreated(product, null);

        ArgumentCaptor<AuditLogEntity> captor = ArgumentCaptor.forClass(AuditLogEntity.class);
        verify(auditLogJpaRepository).save(captor.capture());
        assertThat(captor.getValue().getActor()).isEqualTo("system");
    }

    @Test
    void logUpdated_ShouldCaptureBeforeAndAfterState() {
        Product oldProduct = new Product(productId, "Old Laptop", 500.0, 3);

        auditLogAdapter.logUpdated(product, oldProduct, "admin");

        ArgumentCaptor<AuditLogEntity> captor = ArgumentCaptor.forClass(AuditLogEntity.class);
        verify(auditLogJpaRepository).save(captor.capture());
        AuditLogEntity saved = captor.getValue();

        assertThat(saved.getAction()).isEqualTo("UPDATED");
        assertThat(saved.getDetails())
                .contains("Old Laptop")
                .contains("Laptop")
                .contains("500.00")
                .contains("999.99");
    }

    @Test
    void logDeleted_ShouldSaveAuditLogWithDeletedAction() {
        auditLogAdapter.logDeleted(product, "admin");

        ArgumentCaptor<AuditLogEntity> captor = ArgumentCaptor.forClass(AuditLogEntity.class);
        verify(auditLogJpaRepository).save(captor.capture());
        AuditLogEntity saved = captor.getValue();

        assertThat(saved.getAction()).isEqualTo("DELETED");
        assertThat(saved.getProductId()).isEqualTo(productId);
        assertThat(saved.getDetails()).contains("deleted");
    }

    @Test
    void logStockAdjusted_ShouldCaptureDelta() {
        Product adjusted = new Product(productId, "Laptop", 999.99, 10);

        auditLogAdapter.logStockAdjusted(adjusted, 5, 10, "admin");

        ArgumentCaptor<AuditLogEntity> captor = ArgumentCaptor.forClass(AuditLogEntity.class);
        verify(auditLogJpaRepository).save(captor.capture());
        AuditLogEntity saved = captor.getValue();

        assertThat(saved.getAction()).isEqualTo("STOCK_ADJUSTED");
        assertThat(saved.getDetails())
                .contains("from 5 to 10")
                .contains("delta=5");
    }
}
