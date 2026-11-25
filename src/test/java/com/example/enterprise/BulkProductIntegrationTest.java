package com.example.enterprise;

import com.example.enterprise.interfaces.rest.dto.AuthResponse;
import com.example.enterprise.interfaces.rest.dto.LoginRequest;
import com.example.enterprise.interfaces.rest.dto.ProductBulkRequest;
import com.example.enterprise.interfaces.rest.dto.ProductRequest;
import com.example.enterprise.interfaces.rest.dto.ProductResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BulkProductIntegrationTest {

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

    private HttpHeaders headers() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);
        return headers;
    }

    @Test
    void createBulk_withValidItems_ShouldCreateAll() {
        String prefix = "Bulk-" + UUID.randomUUID() + "-";
        ProductBulkRequest request = new ProductBulkRequest(List.of(
                new ProductRequest(prefix + "A", 10.0, 1),
                new ProductRequest(prefix + "B", 20.0, 2),
                new ProductRequest(prefix + "C", 30.0, 3)
        ));

        ResponseEntity<List<ProductResponse>> response = restTemplate.exchange(
                "/api/products/bulk",
                HttpMethod.POST,
                new HttpEntity<>(request, headers()),
                new ParameterizedTypeReference<>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).hasSize(3);
        assertThat(response.getBody())
                .extracting(ProductResponse::name)
                .containsExactly(prefix + "A", prefix + "B", prefix + "C");
    }

    @Test
    void createBulk_withDuplicateNamesInBatch_ShouldReturn400() {
        String name = "Dup-" + UUID.randomUUID();
        ProductBulkRequest request = new ProductBulkRequest(List.of(
                new ProductRequest(name, 10.0, 1),
                new ProductRequest(name, 20.0, 2)
        ));

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/products/bulk",
                HttpMethod.POST,
                new HttpEntity<>(request, headers()),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void createBulk_withInvalidItem_ShouldRollbackAll() {
        String prefix = "Rollback-" + UUID.randomUUID() + "-";
        ProductBulkRequest request = new ProductBulkRequest(List.of(
                new ProductRequest(prefix + "OK", 10.0, 1),
                new ProductRequest("", 20.0, 2)
        ));

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/products/bulk",
                HttpMethod.POST,
                new HttpEntity<>(request, headers()),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        ResponseEntity<ProductResponse[]> all = restTemplate.exchange(
                "/api/products",
                HttpMethod.GET,
                new HttpEntity<>(headers()),
                ProductResponse[].class);

        assertThat(all.getBody())
                .extracting(ProductResponse::name)
                .doesNotContain(prefix + "OK");
    }

    @Test
    void createBulk_exceeding100Items_ShouldReturn400() {
        List<ProductRequest> items = IntStream.range(0, 101)
                .mapToObj(i -> new ProductRequest("Over-" + UUID.randomUUID() + "-" + i, 10.0, 1))
                .toList();
        ProductBulkRequest request = new ProductBulkRequest(items);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/products/bulk",
                HttpMethod.POST,
                new HttpEntity<>(request, headers()),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void createBulk_withEmptyList_ShouldReturn400() {
        ProductBulkRequest request = new ProductBulkRequest(List.of());

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/products/bulk",
                HttpMethod.POST,
                new HttpEntity<>(request, headers()),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
