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

class RegisterRequestValidationTest {

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
        RegisterRequest request = new RegisterRequest("alice", "supersecret", null);

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void shortUsername_ShouldViolate() {
        RegisterRequest request = new RegisterRequest("ab", "supersecret", null);

        assertThat(validator.validate(request))
                .extracting(v -> v.getPropertyPath().toString())
                .contains("username");
    }

    @Test
    void tooLongUsername_ShouldViolate() {
        String longName = "a".repeat(51);
        RegisterRequest request = new RegisterRequest(longName, "supersecret", null);

        assertThat(validator.validate(request))
                .extracting(v -> v.getPropertyPath().toString())
                .contains("username");
    }

    @Test
    void shortPassword_ShouldViolate() {
        RegisterRequest request = new RegisterRequest("alice", "short", null);

        assertThat(validator.validate(request))
                .extracting(v -> v.getPropertyPath().toString())
                .contains("password");
    }

    @Test
    void tooLongPassword_ShouldViolate() {
        String longPass = "p".repeat(101);
        RegisterRequest request = new RegisterRequest("alice", longPass, null);

        assertThat(validator.validate(request))
                .extracting(v -> v.getPropertyPath().toString())
                .contains("password");
    }

    @Test
    void blankPassword_ShouldViolate() {
        RegisterRequest request = new RegisterRequest("alice", "   ", null);

        assertThat(validator.validate(request))
                .extracting(v -> v.getPropertyPath().toString())
                .contains("password");
    }
}
