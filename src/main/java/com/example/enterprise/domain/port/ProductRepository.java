package com.example.enterprise.domain.port;

import com.example.enterprise.domain.Product;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository {
    Product save(Product product);
    Optional<Product> findById(UUID id);
    List<Product> findAll();
    List<Product> findByNameContaining(String namePart);
    void deleteById(UUID id);
    boolean existsById(UUID id);
}
