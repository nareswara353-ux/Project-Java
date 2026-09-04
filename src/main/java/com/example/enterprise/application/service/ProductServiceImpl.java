package com.example.enterprise.application.service;

import com.example.enterprise.application.port.ProductService;
import com.example.enterprise.domain.Product;
import com.example.enterprise.domain.port.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    public Product createProduct(String name, double price, int stock) {
        UUID id = UUID.randomUUID();
        Product product = new Product(id, name, price, stock);
        return productRepository.save(product);
    }

    @Override
    public Product updateProduct(UUID id, String name, double price, int stock) {
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + id));
        Product updated = new Product(existing.id(), name, price, stock);
        return productRepository.save(updated);
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
    public void deleteProduct(UUID id) {
        if (!productRepository.existsById(id)) {
            throw new IllegalArgumentException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
    }

    @Override
    public Product adjustStock(UUID id, int delta) {
        Product product = getProductById(id);
        int newStock = product.stock() + delta;
        if (newStock < 0) {
            throw new IllegalArgumentException("Stock cannot be negative after adjustment");
        }
        Product adjusted = product.withStock(newStock);
        return productRepository.save(adjusted);
    }
}
