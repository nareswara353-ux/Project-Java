package com.example.enterprise.application.service;

import com.example.enterprise.application.port.EventPublisher;
import com.example.enterprise.application.port.ProductService;
import com.example.enterprise.domain.Product;
import com.example.enterprise.domain.event.ProductEvent;
import com.example.enterprise.domain.exception.ProductNotFoundException;
import com.example.enterprise.domain.port.ProductRepository;
import com.example.enterprise.infrastructure.metrics.ProductMetrics;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static com.example.enterprise.config.CacheConfig.PRODUCTS_CACHE;
import static com.example.enterprise.config.CacheConfig.PRODUCT_SEARCH_CACHE;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final EventPublisher eventPublisher;
    private final ProductMetrics productMetrics;

    @Override
    @Caching(evict = {
            @CacheEvict(value = PRODUCTS_CACHE, allEntries = true),
            @CacheEvict(value = PRODUCT_SEARCH_CACHE, allEntries = true)
    })
    public Product createProduct(String name, double price, int stock) {
        UUID id = UUID.randomUUID();
        Product product = new Product(id, name, price, stock);
        Product saved = productRepository.save(product);
        productMetrics.recordCreate();
        eventPublisher.publish(new ProductEvent.ProductCreated(saved));
        return saved;
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = PRODUCTS_CACHE, allEntries = true),
            @CacheEvict(value = PRODUCT_SEARCH_CACHE, allEntries = true)
    })
    public Product updateProduct(UUID id, String name, double price, int stock) {
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        Product updated = new Product(existing.id(), name, price, stock);
        Product saved = productRepository.save(updated);
        productMetrics.recordUpdate();
        eventPublisher.publish(new ProductEvent.ProductUpdated(saved, existing));
        return saved;
    }

    @Override
    @Cacheable(value = PRODUCTS_CACHE, key = "#id")
    public Product getProductById(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    @Cacheable(value = PRODUCT_SEARCH_CACHE, key = "#namePart == null ? 'ALL' : #namePart.toLowerCase()")
    public List<Product> searchProducts(String namePart) {
        return productRepository.findByNameContaining(namePart);
    }

    @Override
    public long countProducts() {
        return productRepository.count();
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = PRODUCTS_CACHE, allEntries = true),
            @CacheEvict(value = PRODUCT_SEARCH_CACHE, allEntries = true)
    })
    public void deleteProduct(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        productRepository.deleteById(id);
        productMetrics.recordDelete();
        eventPublisher.publish(new ProductEvent.ProductDeleted(id, product));
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = PRODUCTS_CACHE, allEntries = true),
            @CacheEvict(value = PRODUCT_SEARCH_CACHE, allEntries = true)
    })
    public Product adjustStock(UUID id, int delta) {
        Product product = getProductById(id);
        int oldStock = product.stock();
        int newStock = oldStock + delta;
        if (newStock < 0) {
            throw new IllegalArgumentException("Stock cannot be negative after adjustment");
        }
        Product adjusted = product.withStock(newStock);
        Product saved = productRepository.save(adjusted);
        eventPublisher.publish(new ProductEvent.ProductStockAdjusted(saved, oldStock, newStock));
        return saved;
    }
}
