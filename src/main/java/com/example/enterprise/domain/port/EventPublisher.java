package com.example.enterprise.domain.port;

public interface EventPublisher {
    <T> void publish(T event);
}
