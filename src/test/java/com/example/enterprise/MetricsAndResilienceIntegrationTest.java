package com.example.enterprise;

import com.example.enterprise.interfaces.rest.dto.AuthResponse;
import com.example.enterprise.interfaces.rest.dto.LoginRequest;
import com.example.enterprise.interfaces.rest.dto.ProductRequest;
import com.example.enterprise.interfaces.rest.dto.ProductResponse;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
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

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MetricsAndResilienceIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    private String adminToken;

    @BeforeAll
    void login() {
        ResponseEntity<AuthResponse> res = restTemplate.postForEntity(
                "/api/auth/login",
                new LoginRequest("admin", "admin123"),
                AuthResponse.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        adminToken = Objects.requireNonNull(res.getBody()).accessToken();
    }

    private HttpHeaders authHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(adminToken);
        return headers;
    }

    @Test
    void prometheusEndpoint_ShouldExposeCustomProductMetrics() {
        restTemplate.exchange(
                "/api/products",
                HttpMethod.POST,
                new HttpEntity<>(new ProductRequest("Metrics-Test-" + System.nanoTime(), 100.0, 1), authHeaders()),
                ProductResponse.class);

        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/actuator/prometheus", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .contains("products_operations_total")
                .contains("products_total");
    }

    @Test
    void prometheusEndpoint_ShouldExposeJvmMetrics() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/actuator/prometheus", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .contains("jvm_memory_used_bytes")
                .contains("jvm_classes_loaded");
    }

    @Test
    void healthEndpoint_ShouldIncludeCircuitBreakers() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/actuator/health", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("circuitBreakers");
    }

    @Test
    void circuitBreaker_EventPublisherShouldBeRegistered() {
        CircuitBreaker breaker = circuitBreakerRegistry
                .find("eventPublisher")
                .orElseThrow(() -> new AssertionError("Circuit breaker 'eventPublisher' not registered"));

        assertThat(breaker.getState()).isIn(
                CircuitBreaker.State.CLOSED,
                CircuitBreaker.State.HALF_OPEN
        );
        assertThat(breaker.getCircuitBreakerConfig().getSlidingWindowSize()).isEqualTo(10);
        assertThat(breaker.getCircuitBreakerConfig().getFailureRateThreshold()).isEqualTo(50f);
    }

    @Test
    void circuitBreaker_ShouldRemainClosedUnderNormalOperation() {
        for (int i = 0; i < 3; i++) {
            restTemplate.exchange(
                    "/api/products",
                    HttpMethod.POST,
                    new HttpEntity<>(new ProductRequest("CB-Test-" + System.nanoTime(), 50.0, 1), authHeaders()),
                    ProductResponse.class);
        }

        CircuitBreaker breaker = circuitBreakerRegistry.circuitBreaker("eventPublisher");
        assertThat(breaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
    }
}
