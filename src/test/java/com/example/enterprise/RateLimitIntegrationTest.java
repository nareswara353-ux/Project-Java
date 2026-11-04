package com.example.enterprise;

import com.example.enterprise.interfaces.rest.dto.LoginRequest;
import com.example.enterprise.interfaces.rest.dto.ProductRequest;
import com.example.enterprise.interfaces.rest.dto.ProductResponse;
import com.example.enterprise.interfaces.rest.dto.AuthResponse;
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
class RateLimitIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private String adminToken;

    @BeforeAll
    void loginBeforeTests() {
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
    void login_AfterFiveAttempts_ShouldReturn429() {
        restTemplate.postForEntity(
                "/api/auth/login",
                new LoginRequest("ghost-1", "wrong"),
                String.class);

        ResponseEntity<String> sixth = restTemplate.postForEntity(
                "/api/auth/login",
                new LoginRequest("ghost-1", "wrong"),
                String.class);

        assertThat(sixth.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        assertThat(sixth.getBody()).contains("Too Many Requests");
        assertThat(sixth.getHeaders().getFirst("Retry-After")).isNotNull();
    }

    @Test
    void protectedEndpoint_ShouldNotBeRateLimitedByAuthBucket() {
        ResponseEntity<ProductResponse[]> response = restTemplate.exchange(
                "/api/products",
                HttpMethod.GET,
                new HttpEntity<>(authHeaders()),
                ProductResponse[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void createProduct_AfterMultipleCalls_ShouldNotHitAuthRateLimit() {
        for (int i = 0; i < 3; i++) {
            ResponseEntity<ProductResponse> response = restTemplate.exchange(
                    "/api/products",
                    HttpMethod.POST,
                    new HttpEntity<>(new ProductRequest("RateLimit-Test-" + i, 10.0, 1), authHeaders()),
                    ProductResponse.class);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        }
    }

    @Test
    void healthEndpoint_ShouldNotBeRateLimited() {
        for (int i = 0; i < 10; i++) {
            ResponseEntity<String> response = restTemplate.getForEntity("/api/health", String.class);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }
    }
}
