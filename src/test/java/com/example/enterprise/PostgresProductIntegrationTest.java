package com.example.enterprise;

import com.example.enterprise.domain.Product;
import com.example.enterprise.domain.user.Role;
import com.example.enterprise.domain.user.User;
import com.example.enterprise.infrastructure.adapter.ProductRepositoryAdapter;
import com.example.enterprise.infrastructure.adapter.UserRepositoryAdapter;
import com.example.enterprise.interfaces.rest.dto.AuthResponse;
import com.example.enterprise.interfaces.rest.dto.LoginRequest;
import com.example.enterprise.interfaces.rest.dto.ProductRequest;
import com.example.enterprise.interfaces.rest.dto.ProductResponse;
import com.example.enterprise.support.AbstractPostgresIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PostgresProductIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private ProductRepositoryAdapter productRepository;

    @Autowired
    private UserRepositoryAdapter userRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void productRepository_fullLifecycle_ShouldWorkOnRealPostgres() {
        UUID id = UUID.randomUUID();
        Product product = new Product(id, "Postgres Laptop", 999.99, 5);

        Product saved = productRepository.save(product);
        assertThat(saved.id()).isEqualTo(id);

        var found = productRepository.findById(id);
        assertThat(found).isPresent();
        assertThat(found.get().name()).isEqualTo("Postgres Laptop");

        var updated = productRepository.save(found.get().withStock(10));
        assertThat(updated.stock()).isEqualTo(10);

        assertThat(productRepository.count()).isGreaterThanOrEqualTo(1);

        productRepository.deleteById(id);
        assertThat(productRepository.findById(id)).isEmpty();
    }

    @Test
    void productRepository_caseInsensitiveSearch_ShouldWorkOnPostgres() {
        productRepository.save(new Product(UUID.randomUUID(), "Gaming Laptop", 1500.0, 2));
        productRepository.save(new Product(UUID.randomUUID(), "Office Chair", 300.0, 5));

        List<Product> result = productRepository.findByNameContaining("LAPTOP");

        assertThat(result)
                .extracting(Product::name)
                .contains("Gaming Laptop");
    }

    @Test
    void userRepository_withRoles_ShouldPersistElementCollectionOnPostgres() {
        User user = new User(
                UUID.randomUUID(),
                "pg-user-" + System.nanoTime(),
                "hashed-pass",
                Set.of(Role.ADMIN, Role.USER),
                true
        );

        User saved = userRepository.save(user);

        assertThat(saved.roles()).containsExactlyInAnyOrder(Role.ADMIN, Role.USER);

        var reloaded = userRepository.findByUsername(saved.username());
        assertThat(reloaded).isPresent();
        assertThat(reloaded.get().roles()).containsExactlyInAnyOrder(Role.ADMIN, Role.USER);
    }

    @Test
    void productEntity_uniqueNameConstraint_ShouldEnforceOnPostgres() {
        String unique = "Unique-Product-" + System.nanoTime();
        productRepository.save(new Product(UUID.randomUUID(), unique, 10.0, 1));

        assertThatThrownBy(() ->
                productRepository.save(new Product(UUID.randomUUID(), unique, 20.0, 2)))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void fullApiFlow_ShouldWorkEndToEndOnPostgres() {
        ResponseEntity<AuthResponse> login = restTemplate.postForEntity(
                "/api/auth/login",
                new LoginRequest("admin", "admin123"),
                AuthResponse.class);
        assertThat(login.getStatusCode()).isEqualTo(HttpStatus.OK);
        String token = Objects.requireNonNull(login.getBody()).accessToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        ResponseEntity<ProductResponse> created = restTemplate.exchange(
                "/api/products",
                HttpMethod.POST,
                new HttpEntity<>(new ProductRequest("PG-API-Test", 199.99, 3), headers),
                ProductResponse.class);
        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(created.getBody()).isNotNull();

        UUID productId = created.getBody().id();

        ResponseEntity<ProductResponse> fetched = restTemplate.exchange(
                "/api/products/" + productId,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                ProductResponse.class);
        assertThat(fetched.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<Void> deleted = restTemplate.exchange(
                "/api/products/" + productId,
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                Void.class);
        assertThat(deleted.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }
}
