package com.example.enterprise;

import com.example.enterprise.infrastructure.adapter.AuditLogJpaRepository;
import com.example.enterprise.infrastructure.adapter.JpaProductRepository;
import com.example.enterprise.interfaces.rest.dto.AuditLogResponse;
import com.example.enterprise.interfaces.rest.dto.PageResponse;
import com.example.enterprise.interfaces.rest.dto.ProductRequest;
import com.example.enterprise.interfaces.rest.dto.ProductResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class EnterpriseApplicationIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JpaProductRepository jpaProductRepository;

    @Autowired
    private AuditLogJpaRepository auditLogJpaRepository;

    private static UUID createdProductId;

    @BeforeEach
    void cleanDatabase() {
        // Hapus semua data sebelum setiap test agar tidak ada sisa dari test sebelumnya.
        auditLogJpaRepository.deleteAll();
        jpaProductRepository.deleteAll();
    }

    @Test
    @Order(1)
    void healthEndpoint_ShouldReturnUp() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/health", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("UP");
    }

    @Test
    @Order(2)
    void createProduct_ShouldPersistAndReturnCreated() {
        ProductRequest request = new ProductRequest("Integration Test Product", 149.99, 7);
        ResponseEntity<ProductResponse> response = restTemplate.postForEntity(
                "/api/products", request, ProductResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().name()).isEqualTo("Integration Test Product");
        assertThat(response.getBody().price()).isEqualTo(149.99);
        assertThat(response.getBody().stock()).isEqualTo(7);
        createdProductId = response.getBody().id();
    }

    @Test
    @Order(3)
    void getProductById_ShouldReturnPersistedProduct() {
        // Karena database sudah dibersihkan, kita perlu membuat produk terlebih dahulu.
        // Namun karena test diurutkan (@Order), createdProductId dari test #2 sudah ada.
        ResponseEntity<ProductResponse> response = restTemplate.getForEntity(
                "/api/products/" + createdProductId, ProductResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(createdProductId);
        assertThat(response.getBody().name()).isEqualTo("Integration Test Product");
    }

    @Test
    @Order(4)
    void adjustStock_ShouldUpdatePersistedStock() {
        ResponseEntity<ProductResponse> response = restTemplate.exchange(
                "/api/products/" + createdProductId + "/stock?delta=5",
                HttpMethod.PATCH, null, ProductResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().stock()).isEqualTo(12);
    }

    @Test
    @Order(5)
    void searchProducts_ShouldFindByName() {
        ResponseEntity<ProductResponse[]> response = restTemplate.getForEntity(
                "/api/products/search?name=Integration", ProductResponse[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSizeGreaterThanOrEqualTo(1);
    }

    @Test
    @Order(6)
    void auditLogs_ShouldContainEntriesForCreatedProduct() {
        ResponseEntity<PageResponse<AuditLogResponse>> response = restTemplate.exchange(
                "/api/audit-logs/product/" + createdProductId,
                HttpMethod.GET, null,
                new ParameterizedTypeReference<>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        // Sekarang AuditLogPort asli digunakan, jadi tabel audit log akan terisi.
        assertThat(response.getBody().content()).isNotEmpty();
        assertThat(response.getBody().content())
                .extracting(AuditLogResponse::action)
                .contains("CREATED", "STOCK_ADJUSTED");
    }

    @Test
    @Order(7)
    void deleteProduct_ShouldReturnNoContent() {
        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/products/" + createdProductId,
                HttpMethod.DELETE, null, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    @Order(8)
    void getDeletedProduct_ShouldReturnNotFound() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/products/" + createdProductId, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @Order(9)
    void createProduct_WithDuplicateName_ShouldReturnConflictOrBadRequest() {
        // Buat produk dengan nama "Duplicate Product"
        ProductRequest request = new ProductRequest("Duplicate Product", 10.00, 1);
        restTemplate.postForEntity("/api/products", request, ProductResponse.class);

        // Coba buat lagi dengan nama yang sama
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/products",
                new HttpEntity<>(new ProductRequest("Duplicate Product", 20.00, 2)),
                String.class);

        assertThat(response.getStatusCode().is4xxClientError()).isTrue();
    }
}
