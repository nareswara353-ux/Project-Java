package com.example.enterprise.interfaces.rest.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ProductRequestValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void initValidator() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void closeValidator() {
        factory.close();
    }

    @Test
    void validRequest_ShouldHaveNoViolations() {
        ProductRequest request = new ProductRequest("Laptop", 999.99, 5);

        Set<ConstraintViolation<ProductRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void nullName_ShouldViolate() {
        ProductRequest request = new ProductRequest(null, 10.0, 1);

        Set<ConstraintViolation<ProductRequest>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(v -> v.getPropertyPath().toString())
                .contains("name");
    }

    @Test
    void blankName_ShouldViolate() {
        ProductRequest request = new ProductRequest("   ", 10.0, 1);

        Set<ConstraintViolation<ProductRequest>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(v -> v.getPropertyPath().toString())
                .contains("name");
    }

    @Test
    void nonPositivePrice_ShouldViolate() {
        ProductRequest request = new ProductRequest("Laptop", 0.0, 1);

        Set<ConstraintViolation<ProductRequest>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(v -> v.getPropertyPath().toString())
                .contains("price");
    }

    @Test
    void negativeStock_ShouldViolate() {
        ProductRequest request = new ProductRequest("Laptop", 10.0, -1);

        Set<ConstraintViolation<ProductRequest>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(v -> v.getPropertyPath().toString())
                .contains("stock");
    }

    @Test
    void zeroStock_ShouldBeAllowed() {
        ProductRequest request = new ProductRequest("Laptop", 10.0, 0);

        Set<ConstraintViolation<ProductRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }
}
