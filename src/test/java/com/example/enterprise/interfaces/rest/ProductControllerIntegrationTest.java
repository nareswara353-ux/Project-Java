package com.example.enterprise.interfaces.rest;

import com.example.enterprise.application.port.ProductService;
import com.example.enterprise.domain.Product;
import com.example.enterprise.interfaces.rest.dto.ProductRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
class ProductControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

    @Test
    void createProduct_ShouldReturnCreated() throws Exception {
        ProductRequest request = new ProductRequest("New Product", 29.99, 5);
        UUID id = UUID.randomUUID();
        Product product = new Product(id, "New Product", 29.99, 5);

        when(productService.createProduct(any(), any(), any())).thenReturn(product);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("New Product"))
                .andExpect(jsonPath("$.price").value(29.99))
                .andExpect(jsonPath("$.stock").value(5));
    }

    @Test
    void getProduct_WhenExists_ShouldReturnOk() throws Exception {
        UUID id = UUID.randomUUID();
        Product product = new Product(id, "Existing", 49.99, 10);
        when(productService.getProductById(id)).thenReturn(product);

        mockMvc.perform(get("/api/products/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("Existing"));
    }

    @Test
    void getProduct_WhenNotFound_ShouldReturnNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(productService.getProductById(id))
                .thenThrow(new IllegalArgumentException("Product not found"));

        mockMvc.perform(get("/api/products/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllProducts_ShouldReturnList() throws Exception {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        List<Product> products = List.of(
                new Product(id1, "Product 1", 10.0, 2),
                new Product(id2, "Product 2", 20.0, 3)
        );
        when(productService.getAllProducts()).thenReturn(products);

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Product 1"))
                .andExpect(jsonPath("$[1].name").value("Product 2"));
    }

    @Test
    void updateProduct_ShouldReturnOk() throws Exception {
        UUID id = UUID.randomUUID();
        ProductRequest request = new ProductRequest("Updated", 99.99, 20);
        Product updated = new Product(id, "Updated", 99.99, 20);
        when(productService.updateProduct(eq(id), any(), any(), any())).thenReturn(updated);

        mockMvc.perform(put("/api/products/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"));
    }

    @Test
    void deleteProduct_ShouldReturnNoContent() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(productService).deleteProduct(id);

        mockMvc.perform(delete("/api/products/{id}", id))
                .andExpect(status().isNoContent());
    }

    @Test
    void adjustStock_ShouldReturnOk() throws Exception {
        UUID id = UUID.randomUUID();
        Product adjusted = new Product(id, "Product", 50.0, 15);
        when(productService.adjustStock(eq(id), eq(5))).thenReturn(adjusted);

        mockMvc.perform(patch("/api/products/{id}/stock?delta=5", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock").value(15));
    }
}
