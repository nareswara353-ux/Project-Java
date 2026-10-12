package com.example.enterprise.interfaces.rest;

import com.example.enterprise.infrastructure.adapter.AuditLogJpaRepository;
import com.example.enterprise.infrastructure.adapter.entity.AuditLogEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuditLogController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuditLogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuditLogJpaRepository auditLogJpaRepository;

    private AuditLogEntity sampleLog(UUID productId, String action) {
        return AuditLogEntity.builder()
                .id(UUID.randomUUID())
                .action(action)
                .productId(productId)
                .productName("Laptop")
                .details("details")
                .actor("admin")
                .timestamp(Instant.now())
                .build();
    }

    @Test
    void getAllAuditLogs_ShouldReturnPaginatedResponse() throws Exception {
        Page<AuditLogEntity> page = new PageImpl<>(
                List.of(sampleLog(UUID.randomUUID(), "CREATED")),
                PageRequest.of(0, 20),
                1);
        when(auditLogJpaRepository.findAll(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/audit-logs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].action").value("CREATED"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.page").value(0));
    }

    @Test
    void getAllAuditLogs_WithCustomPagination_ShouldPassPageable() throws Exception {
        Page<AuditLogEntity> emptyPage = new PageImpl<>(List.of(), PageRequest.of(2, 5), 0);
        when(auditLogJpaRepository.findAll(any(Pageable.class))).thenReturn(emptyPage);

        mockMvc.perform(get("/api/audit-logs")
                        .param("page", "2")
                        .param("size", "5")
                        .param("sortBy", "action")
                        .param("direction", "ASC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0));
    }

    @Test
    void getAuditLogsByProductId_ShouldReturnFilteredPage() throws Exception {
        UUID productId = UUID.randomUUID();
        Page<AuditLogEntity> page = new PageImpl<>(
                List.of(sampleLog(productId, "STOCK_ADJUSTED")),
                PageRequest.of(0, 20),
                1);
        when(auditLogJpaRepository.findByProductId(eq(productId), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/audit-logs/product/{id}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].action").value("STOCK_ADJUSTED"))
                .andExpect(jsonPath("$.content[0].productId").value(productId.toString()));
    }

    @Test
    void getAllAuditLogs_WithOversizedPageRequest_ShouldClampToMaxSize() throws Exception {
        Page<AuditLogEntity> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 100), 0);
        when(auditLogJpaRepository.findAll(any(Pageable.class))).thenReturn(emptyPage);

        mockMvc.perform(get("/api/audit-logs").param("size", "9999"))
                .andExpect(status().isOk());
    }
}
