package com.example.enterprise.infrastructure.adapter;

import com.example.enterprise.domain.Product;
import com.example.enterprise.domain.exception.ProductNotFoundException;
import com.example.enterprise.domain.port.ProductRepository;
import com.example.enterprise.infrastructure.adapter.entity.ProductEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ProductRepositoryAdapter implements ProductRepository {

    private final JpaProductRepository jpaProductRepository;

    @Override
    public Product save(Product product) {
        ProductEntity entity = ProductEntity.fromDomain(product);
        ProductEntity saved = jpaProductRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public Optional<Product> findById(UUID id) {
        return jpaProductRepository.findByIdAndDeletedFalse(id)
                .map(ProductEntity::toDomain);
    }

    @Override
    public Optional<Product> findByIdIncludingDeleted(UUID id) {
        return jpaProductRepository.findById(id)
                .map(ProductEntity::toDomain);
    }

    @Override
    public List<Product> findAll() {
        return jpaProductRepository.findByDeletedFalse().stream()
                .map(ProductEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Product> findByNameContaining(String namePart) {
        if (namePart == null || namePart.isBlank()) {
            return findAll();
        }
        return jpaProductRepository.findByNameContainingIgnoreCaseAndDeletedFalse(namePart).stream()
                .map(ProductEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public long count() {
        return jpaProductRepository.countByDeletedFalse();
    }

    @Override
    public void deleteById(UUID id) {
        ProductEntity entity = jpaProductRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        entity.setDeleted(true);
        entity.setDeletedAt(Instant.now());
        jpaProductRepository.save(entity);
    }

    @Override
    public void restoreById(UUID id) {
        ProductEntity entity = jpaProductRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        if (!entity.isDeleted()) {
            throw new IllegalStateException("Product is not deleted: " + id);
        }
        entity.setDeleted(false);
        entity.setDeletedAt(null);
        jpaProductRepository.save(entity);
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaProductRepository.findByIdAndDeletedFalse(id).isPresent();
    }
}
