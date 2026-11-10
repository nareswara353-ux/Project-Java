package com.example.enterprise.infrastructure.adapter;

import com.example.enterprise.application.port.EventPublisher;
import com.example.enterprise.domain.event.ProductEvent;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
@RequiredArgsConstructor
@Slf4j
public class ResilientEventPublisher implements EventPublisher {

    private final SpringEventPublisherAdapter delegate;

    @Override
    @CircuitBreaker(name = "eventPublisher", fallbackMethod = "fallbackPublish")
    public void publish(ProductEvent event) {
        delegate.publish(event);
    }

    @SuppressWarnings("unused")
    private void fallbackPublish(ProductEvent event, Throwable t) {
        log.warn("Event publication failed for {} ({}); circuit breaker fallback active. Cause: {}",
                event.getClass().getSimpleName(),
                event.productId(),
                t.getClass().getSimpleName());
    }
}
