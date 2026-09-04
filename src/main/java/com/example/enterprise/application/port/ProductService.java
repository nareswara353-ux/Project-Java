package com.example.enterprise.application.port;

import com.example.enterprise.domain.Product;

import java.util.List;
import java.util.UUID;

public interface ProductService {
    Product createProduct(String name, double price, int stock);
    Product updateProduct(UUID id, String name, double price, int stock);
    Product getProductById(UUID id);
    List<Product> getAllProducts();
    void deleteProduct(UUID id);
    Product adjustStock(UUID id, int delta);
}
