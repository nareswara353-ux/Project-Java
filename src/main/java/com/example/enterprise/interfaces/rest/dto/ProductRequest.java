package com.example.enterprise.interfaces.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record ProductRequest(
        @NotBlank(message = "Product name is required")
        String name,

        @Positive(message = "Price must be greater than zero")
        double price,

        @PositiveOrZero(message = "Stock must be zero or positive")
        int stock
) {}
