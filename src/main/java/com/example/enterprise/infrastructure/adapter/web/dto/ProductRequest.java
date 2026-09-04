package com.example.enterprise.infrastructure.adapter.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProductRequest(
    @NotBlank(message = "Name is required")
    String name,
    
    @NotNull(message = "Price is required")
    @Min(value = 0, message = "Price must be non-negative")
    Double price,
    
    @NotNull(message = "Stock is required")
    @Min(value = 0, message = "Stock must be non-negative")
    Integer stock
) {}
