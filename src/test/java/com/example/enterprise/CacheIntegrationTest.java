package com.example.enterprise;

import com.example.enterprise.interfaces.rest.dto.AuthResponse;
import com.example.enterprise.interfaces.rest.dto.LoginRequest;
import com.example.enterprise.interfaces.rest.dto.ProductRequest;
import com.example.enterprise.interfaces.rest.dto.ProductResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.cache.CacheManager;
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
class CacheIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private CacheManager cacheManager;

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

    @BeforeEach
    void clearCacheBeforeEach() {
        cacheManager.getCacheNames()
                .forEach(name -> Objects.requireNonNull(cacheManager.getCache(name)).clear());
    }

    private HttpHeaders authHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(adminToken);
        return headers;
    }

    private ProductResponse createProduct(String name) {
        ResponseEntity<ProductResponse> res = restTemplate.exchange(
                "/api/products",
                HttpMethod.POST,
                new HttpEntity<>(new ProductRequest(name, 100.0, 5), authHeaders()),
                ProductResponse.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return res.getBody();
    }

    @Test
    void getProductById_ShouldPopulateCache() {
        ProductResponse created = createProduct("Cache-Test-1");
        var cache = cacheManager.getCache("products");
        assertThat(cache).isNotNull();
        cache.clear();

        restTemplate.exchange("/api/products/" + created.id(), HttpMethod.GET,
                new HttpEntity<>(authHeaders()), ProductResponse.class);

        assertThat(cache.get(created.id())).isNotNull();
    }

    @Test
    void createProduct_ShouldEvictProductsCache() {
        createProduct("Cache-Test-2");
        var cache = cacheManager.getCache("products");
        assertThat(cache).isNotNull();

        restTemplate.exchange("/api/products/" + java.util.UUID.randomUUID(), HttpMethod.GET,
                new HttpEntity<>(authHeaders()), String.class);

        createProduct("Cache-Test-3");

        assertThat(cache.get("some-key")).isNull();
    }

    @Test
    void searchProducts_ShouldPopulateSearchCache() {
        createProduct("Cache-Search-A");
        var searchCache = cacheManager.getCache("productSearch");
        assertThat(searchCache).isNotNull();
        searchCache.clear();

        restTemplate.exchange("/api/products/search?name=Cache-Search",
                HttpMethod.GET, new HttpEntity<>(authHeaders()), ProductResponse[].class);

        assertThat(searchCache.get("cache-search")).isNotNull();
    }

    @Test
    void adminCacheClear_ShouldClearAllCaches() {
        ProductResponse created = createProduct("Cache-Clear-1");
        restTemplate.exchange("/api/products/" + created.id(), HttpMethod.GET,
                new HttpEntity<>(authHeaders()), ProductResponse.class);

        ResponseEntity<String> clearRes = restTemplate.exchange(
                "/api/admin/cache/clear",
                HttpMethod.POST,
                new HttpEntity<>(authHeaders()),
                String.class);

        assertThat(clearRes.getStatusCode()).isEqualTo(HttpStatus.OK);
        var cache = cacheManager.getCache("products");
        assertThat(cache).isNotNull();
        assertThat(cache.get(created.id())).isNull();
    }

    @Test
    void adminCacheStats_ShouldReturnCacheNames() {
        ResponseEntity<String> res = restTemplate.exchange(
                "/api/admin/cache/stats",
                HttpMethod.GET,
                new HttpEntity<>(authHeaders()),
                String.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody())
                .contains("\"products\"")
                .contains("\"productSearch\"");
    }
}
