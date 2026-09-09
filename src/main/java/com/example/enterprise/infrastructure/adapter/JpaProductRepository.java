package com.example.enterprise.infrastructure.adapter;

import com.example.enterprise.domain.Product;
import com.example.enterprise.domain.port.ProductRepository;
import com.example.enterprise.infrastructure.adapter.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public interface JpaProductRepository extends JpaRepository<ProductEntity, UUID>, ProductRepository {

    // Custom query method for search by name (case-insensitive)
    List<ProductEntity> findByNameContainingIgnoreCase(String namePart);

    @Override
    default Product save(Product product) {
        ProductEntity entity = ProductEntity.fromDomain(product);
        ProductEntity saved = save(entity);
        return saved.toDomain();
    }

    @Override
    default Optional<Product> findById(UUID id) {
        return findById(id).map(ProductEntity::toDomain);
    }

    @Override
    default List<Product> findAll() {
        return findAll().stream()
                .map(ProductEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    default List<Product> findByNameContaining(String namePart) {
        if (namePart == null || namePart.isBlank()) {
            return findAll();
        }
        return findByNameContainingIgnoreCase(namePart).stream()
                .map(ProductEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    default void deleteById(UUID id) {
        deleteById(id);
    }

    @Override
    default boolean existsById(UUID id) {
        return existsById(id);
    }
}
