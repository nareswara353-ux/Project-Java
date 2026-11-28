package com.example.enterprise.infrastructure.adapter;

import com.example.enterprise.infrastructure.adapter.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaProductRepository extends JpaRepository<ProductEntity, UUID> {

    List<ProductEntity> findByNameContainingIgnoreCase(String namePart);

    Optional<ProductEntity> findByIdAndDeletedFalse(UUID id);

    List<ProductEntity> findByDeletedFalse();

    List<ProductEntity> findByNameContainingIgnoreCaseAndDeletedFalse(String namePart);

    long countByDeletedFalse();
}
