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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.Objects;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class IdempotencyIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private String token;

    @BeforeAll
    void login() {
        ResponseEntity<AuthResponse> res = restTemplate.postForEntity(
                "/api/auth/login",
                new LoginRequest("admin", "admin123"),
                AuthResponse.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        token = Objects.requireNonNull(res.getBody()).accessToken();
    }

    private HttpHeaders headersWithKey(String key) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);
        if (key != null) {
            headers.set("Idempotency-Key", key);
        }
        return headers;
    }

    @Test
    @Order(1)
    void post_withSameKey_ShouldReturnCachedResponse() {
        String idempotencyKey = "test-" + UUID.randomUUID();
        ProductRequest request = new ProductRequest("Idem-Test-" + UUID.randomUUID(), 100.0, 1);

        ResponseEntity<ProductResponse> first = restTemplate.postForEntity(
                "/api/products",
                new HttpEntity<>(request, headersWithKey(idempotencyKey)),
                ProductResponse.class);

        assertThat(first.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(first.getBody()).isNotNull();
        UUID firstId = first.getBody().id();
        assertThat(first.getHeaders().getFirst("X-Idempotency-Replayed")).isNull();

        ResponseEntity<ProductResponse> second = restTemplate.postForEntity(
                "/api/products",
                new HttpEntity<>(request, headersWithKey(idempotencyKey)),
                ProductResponse.class);

        assertThat(second.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(second.getBody()).isNotNull();
        assertThat(second.getBody().id()).isEqualTo(firstId);
        assertThat(second.getHeaders().getFirst("X-Idempotency-Replayed")).isEqualTo("true");
    }

    @Test
    @Order(2)
    void post_withoutKey_ShouldNotCache() {
        ProductRequest request = new ProductRequest("No-Key-" + UUID.randomUUID(), 50.0, 1);

        ResponseEntity<ProductResponse> first = restTemplate.postForEntity(
                "/api/products", new HttpEntity<>(request, headersWithKey(null)), ProductResponse.class);
        ResponseEntity<ProductResponse> second = restTemplate.postForEntity(
                "/api/products", new HttpEntity<>(request, headersWithKey(null)), ProductResponse.class);

        assertThat(first.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(second.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(first.getBody()).isNotNull();
        assertThat(second.getBody()).isNotNull();
        assertThat(first.getBody().id()).isNotEqualTo(second.getBody().id());
    }

    @Test
    @Order(3)
    void post_withInvalidKeyFormat_ShouldReturn400() {
        ProductRequest request = new ProductRequest("Bad-Key-" + UUID.randomUUID(), 10.0, 1);

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/products",
                new HttpEntity<>(request, headersWithKey("ab")),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("Invalid Idempotency-Key");
    }

    @Test
    @Order(4)
    void post_withDifferentKeys_ShouldCreateTwoProducts() {
        String name = "Diff-Key-" + UUID.randomUUID();
        ProductRequest request = new ProductRequest(name, 10.0, 1);

        ResponseEntity<ProductResponse> first = restTemplate.postForEntity(
                "/api/products", new HttpEntity<>(request, headersWithKey("a-" + UUID.randomUUID())),
                ProductResponse.class);
        ResponseEntity<ProductResponse> second = restTemplate.postForEntity(
                "/api/products", new HttpEntity<>(request, headersWithKey("b-" + UUID.randomUUID())),
                ProductResponse.class);

        assertThat(first.getBody()).isNotNull();
        assertThat(second.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(second.getBody()).isNotNull();
    }
}
