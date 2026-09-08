package com.example.enterprise.domain.exception;

public class DuplicateProductException extends RuntimeException {
    public DuplicateProductException(String name) {
        super("Product with name '" + name + "' already exists");
    }

    public DuplicateProductException(String message, Throwable cause) {
        super(message, cause);
    }
}
