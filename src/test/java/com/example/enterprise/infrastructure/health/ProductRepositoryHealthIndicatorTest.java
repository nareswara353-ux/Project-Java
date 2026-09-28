package com.example.enterprise.infrastructure.health;

import com.example.enterprise.domain.port.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductRepositoryHealthIndicatorTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductRepositoryHealthIndicator healthIndicator;

    @Test
    void health_WhenRepositoryAccessible_ShouldReturnUp() {
        when(productRepository.findAll()).thenReturn(List.of());

        Health health = healthIndicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails())
                .containsEntry("repository", "InMemoryProductRepository")
                .containsEntry("status", "available");
    }

    @Test
    void health_WhenRepositoryThrows_ShouldReturnDown() {
        when(productRepository.findAll()).thenThrow(new RuntimeException("DB unreachable"));

        Health health = healthIndicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails())
                .containsEntry("repository", "InMemoryProductRepository")
                .containsEntry("error", "DB unreachable");
    }
}
