package com.example.enterprise.interfaces.rest.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ProductBulkRequest(
        @NotEmpty(message = "Bulk request cannot be empty")
        @Size(max = 100, message = "Bulk request cannot exceed 100 items")
        @Valid
        List<ProductRequest> items
) {}
