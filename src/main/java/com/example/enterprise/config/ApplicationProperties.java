package com.example.enterprise.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app")
@Data
public class ApplicationProperties {
    private int defaultStock = 10;
    private double defaultDiscountRate = 0.0;
    private int maxNameLength = 100;
    private boolean enableMetrics = true;
}
