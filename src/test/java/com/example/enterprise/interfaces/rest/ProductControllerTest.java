package com.example.enterprise.interfaces.rest;

import com.example.enterprise.application.port.ProductService;
import com.example.enterprise.domain.Product;
import com.example.enterprise.domain.port.ProductRepository;
import com.example.enterprise.domain.port.UserRepository;
import com.example.enterprise.infrastructure.security.JwtService;
import com.example.enterprise.interfaces.rest.dto.ProductRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private ProductRepository productRepository;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    void createProduct_ShouldReturnCreated() throws Exception {
        ProductRequest request = new ProductRequest("Laptop", 999.99, 5);
        UUID id = UUID.randomUUID();
        Product product = new Product(id, "Laptop", 999.99, 5);

        when(productService.createProduct(anyString(), anyDouble(), anyInt())).thenReturn(product);
        when(productRepository.findByNameContaining("Laptop")).thenReturn(List.of());

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
        when(productRepository.findByNameContaining("Updated Laptop")).thenReturn(List.of());

        mockMvc.perform(put("/api/products/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Laptop"));
    }

    @Test
    void getProduct_ShouldReturnProduct() throws Exception {
        UUID id = UUID.randomUUID();
        Product product = new Product(id, "Monitor", 199.99, 10);

        when(productService.getProductById(id)).thenReturn(product);

        mockMvc.perform(get("/api/products/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("Monitor"));
    }

    @Test
    void getAllProducts_ShouldReturnList() throws Exception {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        List<Product> products = List.of(
                new Product(id1, "A", 10.0, 1),
                new Product(id2, "B", 20.0, 2)
        );

        when(productService.getAllProducts()).thenReturn(products);

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("A"))
                .andExpect(jsonPath("$[1].name").value("B"));
    }

    @Test
    void deleteProduct_ShouldReturnNoContent() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/products/{id}", id))
                .andExpect(status().isNoContent());
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

    @Test
    void searchProducts_ShouldReturnFilteredList() throws Exception {
        UUID id = UUID.randomUUID();
        List<Product> results = List.of(new Product(id, "Phone", 599.99, 8));

        when(productService.searchProducts("Phone")).thenReturn(results);

        mockMvc.perform(get("/api/products/search").param("name", "Phone"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Phone"));
    }
}
