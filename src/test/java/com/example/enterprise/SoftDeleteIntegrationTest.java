package com.example.enterprise;

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

import java.util.Objects;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SoftDeleteIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private String adminToken;
    private static UUID productId;

    @BeforeAll
    void login() {
        ResponseEntity<AuthResponse> res = restTemplate.postForEntity(
                "/api/auth/login",
                new LoginRequest("admin", "admin123"),
                AuthResponse.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        adminToken = Objects.requireNonNull(res.getBody()).accessToken();
    }

    private HttpHeaders headers() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(adminToken);
        return headers;
    }

    @Test
    @Order(1)
    void createProduct_ShouldBeVisibleInList() {
        ResponseEntity<ProductResponse> created = restTemplate.exchange(
                "/api/products",
                HttpMethod.POST,
                new HttpEntity<>(new ProductRequest("SoftDelete-Test", 42.0, 1), headers()),
                ProductResponse.class);

        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(created.getBody()).isNotNull();
        productId = created.getBody().id();
    }

    @Test
    @Order(2)
    void deleteProduct_ShouldReturn204AndHideFromGet() {
        ResponseEntity<Void> deleted = restTemplate.exchange(
                "/api/products/" + productId,
                HttpMethod.DELETE,
                new HttpEntity<>(headers()),
                Void.class);

        assertThat(deleted.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<String> getAfter = restTemplate.exchange(
                "/api/products/" + productId,
                HttpMethod.GET,
                new HttpEntity<>(headers()),
                String.class);

        assertThat(getAfter.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @Order(3)
    void deletedProduct_ShouldNotAppearInSearch() {
        ResponseEntity<ProductResponse[]> search = restTemplate.exchange(
                "/api/products/search?name=SoftDelete",
                HttpMethod.GET,
                new HttpEntity<>(headers()),
                ProductResponse[].class);

        assertThat(search.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(search.getBody()).isNotNull();
        assertThat(search.getBody())
                .extracting(ProductResponse::id)
                .doesNotContain(productId);
    }

    @Test
    @Order(4)
    void deletedProduct_ShouldNotBeCountedInTotal() {
        ResponseEntity<String> count = restTemplate.exchange(
                "/api/products/count",
                HttpMethod.GET,
                new HttpEntity<>(headers()),
                String.class);

        assertThat(count.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @Order(5)
    void restoreProduct_ShouldBringItBack() {
        ResponseEntity<ProductResponse> restored = restTemplate.exchange(
                "/api/products/" + productId + "/restore",
                HttpMethod.POST,
                new HttpEntity<>(headers()),
                ProductResponse.class);

        assertThat(restored.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(restored.getBody()).isNotNull();
        assertThat(restored.getBody().id()).isEqualTo(productId);

        ResponseEntity<ProductResponse> getAfter = restTemplate.exchange(
                "/api/products/" + productId,
                HttpMethod.GET,
                new HttpEntity<>(headers()),
                ProductResponse.class);

        assertThat(getAfter.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @Order(6)
    void restoreProduct_WhenNotDeleted_ShouldReturn5xxOr4xx() {
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/products/" + productId + "/restore",
                HttpMethod.POST,
                new HttpEntity<>(headers()),
                String.class);

        assertThat(response.getStatusCode().isError()).isTrue();
    }

    @Test
    @Order(7)
    void deleteNonExistentProduct_ShouldReturn404() {
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/products/" + UUID.randomUUID(),
                HttpMethod.DELETE,
                new HttpEntity<>(headers()),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
