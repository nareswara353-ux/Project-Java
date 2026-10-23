package com.example.enterprise;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ActuatorIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void healthEndpoint_ShouldReturnUpWithCustomIndicators() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/actuator/health", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .contains("\"status\":\"UP\"")
                .contains("\"productRepositoryHealthIndicator\"")
                .contains("\"jwtHealthIndicator\"");
    }

    @Test
    void healthEndpoint_ShouldExposeJwtHealthDetails() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/actuator/health", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .contains("\"issuer\":\"java-enterprise-portfolio\"")
                .contains("\"expirationMs\":3600000");
    }

    @Test
    void infoEndpoint_ShouldExposeApplicationMetadata() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/actuator/info", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .contains("\"application\"")
                .contains("\"javaVersion\"");
    }

    @Test
    void infoEndpoint_ShouldExposeBuildOrGitInfo() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/actuator/info", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .satisfiesAnyOf(
                        body -> assertThat(body).contains("\"build\""),
                        body -> assertThat(body).contains("\"git\"")
                );
    }

    @Test
    void metricsEndpoint_ShouldBeAccessible() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/actuator/metrics", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"names\"");
    }
}
