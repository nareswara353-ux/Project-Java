package com.example.enterprise.infrastructure.metrics;

import com.example.enterprise.domain.port.ProductRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Component
public class ProductMetrics {

    private final AtomicLong createCount = new AtomicLong();
    private final AtomicLong updateCount = new AtomicLong();
    private final AtomicLong deleteCount = new AtomicLong();

    private final Counter productCreated;
    private final Counter productUpdated;
    private final Counter productDeleted;

    public ProductMetrics(MeterRegistry registry, ProductRepository productRepository) {
        this.productCreated = Counter.builder("products.operations")
                .tag("operation", "create")
                .description("Total number of product create operations")
                .register(registry);

        this.productUpdated = Counter.builder("products.operations")
                .tag("operation", "update")
                .description("Total number of product update operations")
                .register(registry);

        this.productDeleted = Counter.builder("products.operations")
                .tag("operation", "delete")
                .description("Total number of product delete operations")
                .register(registry);

        Gauge.builder("products.total", productRepository, repo -> {
                    try {
                        return repo.count();
                    } catch (Exception ex) {
                        return -1.0;
                    }
                })
                .description("Total products in database")
                .register(registry);

        Gauge.builder("products.operations.local", createCount, AtomicLong::doubleValue)
                .tag("operation", "create")
                .description("Local counter for create ops")
                .register(registry);

        Gauge.builder("products.operations.local", updateCount, AtomicLong::doubleValue)
                .tag("operation", "update")
                .description("Local counter for update ops")
                .register(registry);

        Gauge.builder("products.operations.local", deleteCount, AtomicLong::doubleValue)
                .tag("operation", "delete")
                .description("Local counter for delete ops")
                .register(registry);
    }

    public void recordCreate() {
        productCreated.increment();
        createCount.incrementAndGet();
    }

    public void recordUpdate() {
        productUpdated.increment();
        updateCount.incrementAndGet();
    }

    public void recordDelete() {
        productDeleted.increment();
        deleteCount.incrementAndGet();
    }
}
