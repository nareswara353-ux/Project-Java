package com.example.enterprise.interfaces.rest.dto;

import jakarta.validation.constraints.NotNull;

public record StockAdjustmentRequest(
        @NotNull(message = "Delta is required")
        Integer delta
) {}
