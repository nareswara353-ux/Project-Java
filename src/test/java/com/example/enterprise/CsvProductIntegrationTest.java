package com.example.enterprise;

import com.example.enterprise.interfaces.rest.dto.AuthResponse;
import com.example.enterprise.interfaces.rest.dto.LoginRequest;
import com.example.enterprise.interfaces.rest.dto.ProductRequest;
import com.example.enterprise.interfaces.rest.dto.ProductResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CsvProductIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

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
        headers.setBearerAuth(adminToken);
        return headers;
    }

    private HttpEntity<MultiValueMap<String, Object>> multipartWith(byte[] csvContent) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.setBearerAuth(adminToken);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ByteArrayResource(csvContent) {
            @Override
            public String getFilename() {
                return "import.csv";
            }
        });

        return new HttpEntity<>(body, headers);
    }

    @Test
    void exportProducts_ShouldReturnCsvWithHeaders() {
        restTemplate.exchange(
                "/api/products",
                HttpMethod.POST,
                new HttpEntity<>(new ProductRequest("CSV-Export-" + UUID.randomUUID(), 99.99, 3), authHeaders()),
                ProductResponse.class);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/products/export",
                HttpMethod.GET,
                new HttpEntity<>(authHeaders()),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isNotNull();
        assertThat(Objects.requireNonNull(response.getHeaders().getContentType()).toString())
                .contains("text/csv");
        assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION))
                .contains("attachment")
                .contains(".csv");
        assertThat(response.getBody())
                .contains("id,name,price,stock");
    }

    @Test
    void importProducts_withValidCsv_ShouldCreateAll() {
        String prefix = "CSV-Import-" + UUID.randomUUID() + "-";
        String csv = "name,price,stock\n"
                + prefix + "A,10.50,1\n"
                + prefix + "B,20.75,2\n"
                + prefix + "C,30.25,3\n";

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/products/import",
                HttpMethod.POST,
                multipartWith(csv.getBytes(StandardCharsets.UTF_8)),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"created\":3");

        ResponseEntity<ProductResponse[]> all = restTemplate.exchange(
                "/api/products/search?name=" + prefix,
                HttpMethod.GET,
                new HttpEntity<>(authHeaders()),
                ProductResponse[].class);
        assertThat(all.getBody()).hasSize(3);
    }

    @Test
    void importProducts_withInvalidRow_ShouldReturn400AndRollback() {
        String prefix = "CSV-Bad-" + UUID.randomUUID() + "-";
        String csv = "name,price,stock\n"
                + prefix + "OK,10.0,1\n"
                + prefix + "Bad,not-a-number,2\n";

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/products/import",
                HttpMethod.POST,
                multipartWith(csv.getBytes(StandardCharsets.UTF_8)),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("\"errors\"");

        ResponseEntity<ProductResponse[]> all = restTemplate.exchange(
                "/api/products/search?name=" + prefix,
                HttpMethod.GET,
                new HttpEntity<>(authHeaders()),
                ProductResponse[].class);
        assertThat(all.getBody()).isEmpty();
    }

    @Test
    void importProducts_withMissingHeaders_ShouldReturn400() {
        String csv = "foo,bar\n1,2\n";

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/products/import",
                HttpMethod.POST,
                multipartWith(csv.getBytes(StandardCharsets.UTF_8)),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("headers");
    }

    @Test
    void importProducts_withEmptyFile_ShouldReturn400() {
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/products/import",
                HttpMethod.POST,
                multipartWith(new byte[0]),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void importProducts_withOnlyHeader_ShouldReturn400() {
        String csv = "name,price,stock\n";

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/products/import",
                HttpMethod.POST,
                multipartWith(csv.getBytes(StandardCharsets.UTF_8)),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("No data rows");
    }
}
