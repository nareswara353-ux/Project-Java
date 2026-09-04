package com.example.enterprise.interfaces.rest;

import com.example.enterprise.application.port.ProductService;
import com.example.enterprise.domain.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestParam String name,
                                                  @RequestParam double price,
                                                  @RequestParam int stock) {
        Product product = productService.createProduct(name, price, stock);
        return ResponseEntity.status(HttpStatus.CREATED).body(product);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable UUID id,
                                                  @RequestParam String name,
                                                  @RequestParam double price,
                                                  @RequestParam int stock) {
        Product product = productService.updateProduct(id, name, price, stock);
        return ResponseEntity.ok(product);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProduct(@PathVariable UUID id) {
        Product product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }

    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable UUID id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/stock")
    public ResponseEntity<Product> adjustStock(@PathVariable UUID id,
                                               @RequestParam int delta) {
        Product product = productService.adjustStock(id, delta);
        return ResponseEntity.ok(product);
    }
}
