package com.example.enterprise.interfaces.rest.validation;

import com.example.enterprise.domain.port.ProductRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UniqueProductNameValidator implements ConstraintValidator<UniqueProductName, String> {

    private final ProductRepository productRepository;

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true; // handled by @NotBlank
        }
        return productRepository.findByNameContaining(value).stream()
                .noneMatch(p -> p.name().equalsIgnoreCase(value));
    }
}
