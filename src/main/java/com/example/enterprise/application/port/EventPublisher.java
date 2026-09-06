package com.example.enterprise.application.port;

import com.example.enterprise.domain.event.ProductEvent;

public interface EventPublisher {
    void publish(ProductEvent event);
}
