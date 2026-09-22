package com.example.enterprise.interfaces.rest;

import com.example.enterprise.interfaces.rest.dto.AuthResponse;
import com.example.enterprise.interfaces.rest.dto.LoginRequest;
import com.example.enterprise.interfaces.rest.dto.RegisterRequest;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AuthControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private static String accessToken;
    private static String refreshToken;

    @Test
    @Order(1)
    void login_WithDefaultAdmin_ShouldReturnTokens() {
        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
                "/api/auth/login",
                new LoginRequest("admin", "admin123"),
                AuthResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().accessToken()).isNotBlank();
        assertThat(response.getBody().refreshToken()).isNotBlank();
        assertThat(response.getBody().tokenType()).isEqualTo("Bearer");
        assertThat(response.getBody().username()).isEqualTo("admin");

        accessToken = response.getBody().accessToken();
        refreshToken = response.getBody().refreshToken();
    }

    @Test
    @Order(2)
    void login_WithWrongPassword_ShouldReturnUnauthorized() {
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/auth/login",
                new LoginRequest("admin", "wrong-password"),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @Order(3)
    void login_WithUnknownUser_ShouldReturnUnauthorized() {
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/auth/login",
                new LoginRequest("ghost-user", "any"),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @Order(4)
    void register_WithNewUser_ShouldReturnCreated() {
        RegisterRequest request = new RegisterRequest(
                "it-user-" + System.nanoTime(),
                "secret123",
                null
        );

        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
                "/api/auth/register",
                request,
                AuthResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().accessToken()).isNotBlank();
    }

    @Test
    @Order(5)
    void register_WithDuplicateUsername_ShouldReturnClientError() {
        RegisterRequest request = new RegisterRequest("dupe-user", "secret123", null);
        restTemplate.postForEntity("/api/auth/register", request, AuthResponse.class);

        ResponseEntity<String> second = restTemplate.postForEntity(
                "/api/auth/register",
                new HttpEntity<>(new RegisterRequest("dupe-user", "another-pass", null)),
                String.class);

        assertThat(second.getStatusCode().is4xxClientError()).isTrue();
    }

    @Test
    @Order(6)
    void register_WithShortPassword_ShouldReturnBadRequest() {
        RegisterRequest request = new RegisterRequest("short-pass-user", "abc", null);

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/auth/register",
                request,
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @Order(7)
    void refresh_WithValidRefreshToken_ShouldReturnNewAccessToken() {
        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
                "/api/auth/refresh?refreshToken=" + refreshToken,
                null,
                AuthResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().accessToken()).isNotBlank();
        assertThat(response.getBody().username()).isEqualTo("admin");
    }

    @Test
    @Order(8)
    void refresh_WithAccessTokenInstead_ShouldReturnUnauthorized() {
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/auth/refresh?refreshToken=" + accessToken,
                null,
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @Order(9)
    void refresh_WithGarbageToken_ShouldReturnUnauthorized() {
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/auth/refresh?refreshToken=not-a-real-jwt",
                null,
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
