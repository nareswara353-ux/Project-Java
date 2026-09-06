package com.example.enterprise.infrastructure.health;

import com.example.enterprise.domain.port.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductRepositoryHealthIndicator implements HealthIndicator {

    private final ProductRepository productRepository;

    @Override
    public Health health() {
        try {
            // Simple check: attempt to get all products (or just check if repository responds)
            productRepository.findAll();
            return Health.up()
                    .withDetail("repository", "InMemoryProductRepository")
                    .withDetail("status", "available")
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("repository", "InMemoryProductRepository")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
