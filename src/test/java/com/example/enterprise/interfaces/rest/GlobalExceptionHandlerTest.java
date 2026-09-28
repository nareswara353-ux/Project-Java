package com.example.enterprise.interfaces.rest;

import com.example.enterprise.domain.exception.DuplicateProductException;
import com.example.enterprise.domain.exception.ProductNotFoundException;
import com.example.enterprise.interfaces.rest.dto.ErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void handleProductNotFound_ShouldReturn404() {
        UUID id = UUID.randomUUID();
        ProductNotFoundException ex = new ProductNotFoundException(id);

        ResponseEntity<ErrorResponse> response = handler.handleProductNotFound(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(404);
        assertThat(response.getBody().message()).contains(id.toString());
    }

    @Test
    void handleDuplicateProduct_ShouldReturn409() {
        DuplicateProductException ex = new DuplicateProductException("Laptop");

        ResponseEntity<ErrorResponse> response = handler.handleDuplicateProduct(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(409);
        assertThat(response.getBody().message()).contains("Laptop");
    }

    @Test
    void handleIllegalArgument_ShouldReturn400() {
        IllegalArgumentException ex = new IllegalArgumentException("Stock cannot be negative");

        ResponseEntity<ErrorResponse> response = handler.handleIllegalArgument(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(400);
        assertThat(response.getBody().message()).contains("Stock cannot be negative");
    }

    @Test
    void handleValidationErrors_ShouldReturn400WithFieldErrors() {
        Object target = new Object();
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(target, "productRequest");
        bindingResult.addError(new FieldError("productRequest", "name", "Product name is required"));
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<ErrorResponse> response = handler.handleValidationErrors(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(400);
        assertThat(response.getBody().message()).isEqualTo("Validation failed");
        assertThat(response.getBody().validationErrors())
                .isNotNull()
                .hasSize(1)
                .first()
                .extracting(e -> e.get("field"))
                .isEqualTo("name");
    }

    @Test
    void handleMessageNotReadable_ShouldReturn400() {
        HttpMessageNotReadableException ex =
                new HttpMessageNotReadableException("Malformed JSON", mock(org.springframework.http.HttpInputMessage.class));

        ResponseEntity<ErrorResponse> response = handler.handleMessageNotReadable(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(400);
        assertThat(response.getBody().message()).contains("Malformed JSON");
    }

    @Test
    void handleGeneric_ShouldReturn500() {
        Exception ex = new RuntimeException("boom");

        ResponseEntity<ErrorResponse> response = handler.handleGeneric(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(500);
        assertThat(response.getBody().message()).contains("unexpected error");
    }

    @Test
    void handleGeneric_WithNestedCause_ShouldExposeRootCause() {
        Exception root = new IllegalStateException("root cause");
        Exception wrapper = new RuntimeException("wrapper", root);

        ResponseEntity<ErrorResponse> response = handler.handleGeneric(wrapper);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).contains("root cause");
    }
}
