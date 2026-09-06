package com.example.enterprise.infrastructure.adapter;

import com.example.enterprise.application.port.EventPublisher;
import com.example.enterprise.domain.event.ProductEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SpringEventPublisherAdapter implements EventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void publish(ProductEvent event) {
        applicationEventPublisher.publishEvent(event);
    }
}
