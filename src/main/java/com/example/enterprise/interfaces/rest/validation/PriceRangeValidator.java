package com.example.enterprise.interfaces.rest.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PriceRangeValidator implements ConstraintValidator<PriceRange, Double> {

    private double min;
    private double max;

    @Override
    public void initialize(PriceRange constraintAnnotation) {
        this.min = constraintAnnotation.min();
        this.max = constraintAnnotation.max();
    }

    @Override
    public boolean isValid(Double value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // handled by @NotNull if needed
        }
        return value >= min && value <= max;
    }
}
