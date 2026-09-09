package com.example.enterprise.interfaces.rest;

import com.example.enterprise.application.port.ProductService;
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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
class ProductControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private ProductRepository productRepository; // required for validator context

    @Test
    void createProduct_ShouldReturnCreated() throws Exception {
        ProductRequest request = new ProductRequest("Integration Laptop", 799.99, 3);
        when(productService.createProduct(anyString(), anyDouble(), anyInt()))
                .thenReturn(null); // not actually used, just to avoid NPE

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void updateProduct_ShouldReturnOk() throws Exception {
        UUID id = UUID.randomUUID();
        ProductRequest request = new ProductRequest("Updated Integration", 899.99, 5);
        when(productService.updateProduct(eq(id), anyString(), anyDouble(), anyInt()))
                .thenReturn(null);

        mockMvc.perform(put("/api/products/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void getProduct_WhenNotFound_ShouldReturnNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(productService.getProductById(id))
                .thenThrow(new ProductNotFoundException(id)); // 404

        mockMvc.perform(get("/api/products/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteProduct_WhenNotFound_ShouldReturnNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new ProductNotFoundException(id))
                .when(productService).deleteProduct(id);

        mockMvc.perform(delete("/api/products/{id}", id))
                .andExpect(status().isNotFound()); // 404, bukan 204
    }
}
