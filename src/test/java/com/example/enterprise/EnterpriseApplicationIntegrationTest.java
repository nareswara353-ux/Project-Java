package com.example.enterprise;

import com.example.enterprise.infrastructure.adapter.AuditLogJpaRepository;
import com.example.enterprise.infrastructure.adapter.JpaProductRepository;
import com.example.enterprise.interfaces.rest.dto.AuthResponse;
import com.example.enterprise.interfaces.rest.dto.LoginRequest;
import com.example.enterprise.interfaces.rest.dto.ProductRequest;
import com.example.enterprise.interfaces.rest.dto.ProductResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EnterpriseApplicationIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JpaProductRepository jpaProductRepository;

    @Autowired
    private AuditLogJpaRepository auditLogJpaRepository;

    private String adminToken;
    private UUID createdProductId;

    @BeforeAll
    void initOnce() {
        auditLogJpaRepository.deleteAll();
        jpaProductRepository.deleteAll();
        adminToken = login("admin", "admin123");
    }

    private String login(String username, String password) {
        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
                "/api/auth/login",
                new LoginRequest(username, password),
                AuthResponse.class);

        if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
            throw new IllegalStateException("Login failed for user " + username + ": " + response.getStatusCode());
        }
        return response.getBody().accessToken();
    }

    private HttpHeaders authHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(adminToken);
        return headers;
    }

    private <T> void dumpIfError(ResponseEntity<T> response, String label) {
        if (response.getStatusCode().isError()) {
            System.out.println("=========================================================");
            System.out.println("[ERROR RESPONSE] " + label);
            System.out.println("Status : " + response.getStatusCode());
            System.out.println("Body   : " + response.getBody());
            System.out.println("=========================================================");
        }
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
        ResponseEntity<ProductResponse> response = restTemplate.exchange(
                "/api/products",
                HttpMethod.POST,
                new HttpEntity<>(request, authHeaders()),
                ProductResponse.class);
        dumpIfError(response, "POST /api/products");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().name()).isEqualTo("Integration Test Product");
        createdProductId = response.getBody().id();
    }

    @Test
    @Order(3)
    void getProductById_ShouldReturnPersistedProduct() {
        ResponseEntity<ProductResponse> response = restTemplate.exchange(
                "/api/products/" + createdProductId,
                HttpMethod.GET,
                new HttpEntity<>(authHeaders()),
                ProductResponse.class);
        dumpIfError(response, "GET /api/products/" + createdProductId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(createdProductId);
    }

    @Test
    @Order(4)
    void adjustStock_ShouldUpdatePersistedStock() {
        ResponseEntity<ProductResponse> response = restTemplate.exchange(
                "/api/products/" + createdProductId + "/stock?delta=5",
                HttpMethod.PATCH,
                new HttpEntity<>(authHeaders()),
                ProductResponse.class);
        dumpIfError(response, "PATCH /api/products/" + createdProductId + "/stock");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().stock()).isEqualTo(12);
    }

    @Test
    @Order(5)
    void searchProducts_ShouldFindByName() {
        ResponseEntity<ProductResponse[]> response = restTemplate.exchange(
                "/api/products/search?name=Integration",
                HttpMethod.GET,
                new HttpEntity<>(authHeaders()),
                ProductResponse[].class);
        dumpIfError(response, "GET /api/products/search");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSizeGreaterThanOrEqualTo(1);
    }

    @Test
    @Order(6)
    void auditLogs_ShouldContainEntriesForCreatedProduct() {
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/audit-logs/product/" + createdProductId,
                HttpMethod.GET,
                new HttpEntity<>(authHeaders()),
                String.class);
        dumpIfError(response, "GET /api/audit-logs/product/" + createdProductId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("CREATED");
    }

    @Test
    @Order(7)
    void deleteProduct_ShouldReturnNoContent() {
        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/products/" + createdProductId,
                HttpMethod.DELETE,
                new HttpEntity<>(authHeaders()),
                Void.class);
        dumpIfError(response, "DELETE /api/products/" + createdProductId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    @Order(8)
    void getDeletedProduct_ShouldReturnNotFound() {
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/products/" + createdProductId,
                HttpMethod.GET,
                new HttpEntity<>(authHeaders()),
                String.class);
        dumpIfError(response, "GET (deleted) /api/products/" + createdProductId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @Order(9)
    void createProduct_WithDuplicateName_ShouldReturnConflictOrBadRequest() {
        ProductRequest request = new ProductRequest("Duplicate Product", 10.00, 1);
        restTemplate.exchange(
                "/api/products",
                HttpMethod.POST,
                new HttpEntity<>(request, authHeaders()),
                ProductResponse.class);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/products",
                HttpMethod.POST,
                new HttpEntity<>(new ProductRequest("Duplicate Product", 20.00, 2), authHeaders()),
                String.class);
        dumpIfError(response, "POST /api/products (duplicate)");

        assertThat(response.getStatusCode().is4xxClientError()).isTrue();
    }

    @Test
    @Order(10)
    void protectedEndpoint_WithoutToken_ShouldReturnUnauthorized() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/products", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
