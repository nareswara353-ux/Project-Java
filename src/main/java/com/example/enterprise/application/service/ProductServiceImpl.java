package com.example.enterprise.application.service;

import com.example.enterprise.application.port.EventPublisher;
import com.example.enterprise.application.port.ProductService;
import com.example.enterprise.domain.Product;
import com.example.enterprise.domain.event.ProductEvent;
import com.example.enterprise.domain.port.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final EventPublisher eventPublisher;

    @Override
    public Product createProduct(String name, double price, int stock) {
        UUID id = UUID.randomUUID();
        Product product = new Product(id, name, price, stock);
        Product saved = productRepository.save(product);
        eventPublisher.publish(new ProductEvent.ProductCreated(saved));
        return saved;
    }

    @Override
    public Product updateProduct(UUID id, String name, double price, int stock) {
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + id));
        Product updated = new Product(existing.id(), name, price, stock);
        Product saved = productRepository.save(updated);
        eventPublisher.publish(new ProductEvent.ProductUpdated(saved, existing));
        return saved;
    }

    @Override
    public Product getProductById(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + id));
    }

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public List<Product> searchProducts(String namePart) {
        return productRepository.findByNameContaining(namePart);
    }

    @Override
    public void deleteProduct(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + id));
        productRepository.deleteById(id);
        eventPublisher.publish(new ProductEvent.ProductDeleted(id, product));
    }

    @Override
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
