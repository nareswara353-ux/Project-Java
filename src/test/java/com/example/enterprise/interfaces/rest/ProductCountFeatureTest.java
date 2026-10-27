package com.example.enterprise.interfaces.rest;

import com.example.enterprise.application.port.ProductService;
import com.example.enterprise.domain.port.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProductCountFeatureTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private ProductRepository productRepository;

    @Test
    void countEndpoint_WhenZero_ShouldReturnZero() throws Exception {
        when(productService.countProducts()).thenReturn(0L);

        mockMvc.perform(get("/api/products/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(0));
    }

    @Test
    void countEndpoint_WhenMultiple_ShouldReturnCorrectNumber() throws Exception {
        when(productService.countProducts()).thenReturn(42L);

        mockMvc.perform(get("/api/products/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(42));
    }
}
