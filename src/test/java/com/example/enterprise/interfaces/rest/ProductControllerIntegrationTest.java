package com.example.enterprise.interfaces.rest;

import com.example.enterprise.application.port.ProductService;
import com.example.enterprise.domain.Product;
import com.example.enterprise.domain.exception.ProductNotFoundException;
import com.example.enterprise.domain.port.ProductRepository;
import com.example.enterprise.interfaces.rest.dto.ProductRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
public class ProductControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private ProductRepository productRepository;

    @Test
    void createProduct_ShouldReturnCreated() throws Exception {
        ProductRequest request = new ProductRequest("Laptop", 999.99, 5);
        UUID id = UUID.randomUUID();
        Product product = new Product(id, "Laptop", 999.99, 5);

        // Use anyString, anyDouble, anyInt
        when(productService.createProduct(anyString(), anyDouble(), anyInt())).thenReturn(product);
        // Mock repository for validator
        when(productRepository.findByNameContaining("Laptop")).thenReturn(java.util.List.of());

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("Laptop"))
                .andExpect(jsonPath("$.price").value(999.99))
                .andExpect(jsonPath("$.stock").value(5));
    }

    @Test
    void updateProduct_ShouldReturnOk() throws Exception {
        UUID id = UUID.randomUUID();
        ProductRequest request = new ProductRequest("Updated Laptop", 899.99, 3);
        Product updated = new Product(id, "Updated Laptop", 899.99, 3);

        when(productService.updateProduct(eq(id), anyString(), anyDouble(), anyInt())).thenReturn(updated);
        when(productRepository.findByNameContaining("Updated Laptop")).thenReturn(java.util.List.of());

        mockMvc.perform(put("/api/products/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Laptop"));
    }

    @Test
    void getProduct_WhenNotFound_ShouldReturnNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        // Throw ProductNotFoundException, not IllegalArgumentException
        when(productService.getProductById(id))
                .thenThrow(new ProductNotFoundException(id));

        mockMvc.perform(get("/api/products/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Product not found with id: " + id));
    }

    @Test
    void getProduct_WhenExists_ShouldReturnProduct() throws Exception {
        UUID id = UUID.randomUUID();
        Product product = new Product(id, "Monitor", 199.99, 10);
        when(productService.getProductById(id)).thenReturn(product);

        mockMvc.perform(get("/api/products/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("Monitor"));
    }

    @Test
    void deleteProduct_WhenNotFound_ShouldThrowException() throws Exception {
        UUID id = UUID.randomUUID();
        // Simulate delete product throws exception
        when(productService.getProductById(id)).thenThrow(new ProductNotFoundException(id));

        mockMvc.perform(delete("/api/products/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void adjustStock_ShouldReturnUpdatedProduct() throws Exception {
        UUID id = UUID.randomUUID();
        Product adjusted = new Product(id, "Keyboard", 49.99, 12);
        when(productService.adjustStock(eq(id), eq(2))).thenReturn(adjusted);

        mockMvc.perform(patch("/api/products/{id}/stock", id)
                        .param("delta", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock").value(12));
    }
}
