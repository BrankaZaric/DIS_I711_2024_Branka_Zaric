package com.ecommerce.product.controller;

import com.ecommerce.product.dto.ProductRequest;
import com.ecommerce.product.dto.ProductResponse;
import com.ecommerce.product.model.Product;
import com.ecommerce.product.repository.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProductControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    private ProductRequest testProductRequest;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();

        testProductRequest = ProductRequest.builder()
                .sku("INT-TEST-001")
                .name("Integration Test Product")
                .description("Test Description")
                .price(new BigDecimal("99.99"))
                .stockQuantity(100)
                .category("Electronics")
                .imageUrl("http://test.com/image.jpg")
                .active(true)
                .build();
    }

    @AfterEach
    void tearDown() {
        productRepository.deleteAll();
    }

    @Test
    void createProduct_Success() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testProductRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sku").value("INT-TEST-001"))
                .andExpect(jsonPath("$.name").value("Integration Test Product"))
                .andExpect(jsonPath("$.price").value(99.99))
                .andExpect(jsonPath("$.stockQuantity").value(100))
                .andReturn();

        String content = result.getResponse().getContentAsString();
        ProductResponse response = objectMapper.readValue(content, ProductResponse.class);

        assertThat(productRepository.findById(response.getId())).isPresent();
    }

    @Test
    void createProduct_DuplicateSku_Returns409() throws Exception {
        Product existingProduct = Product.builder()
                .sku("INT-TEST-001")
                .name("Existing Product")
                .price(new BigDecimal("50.00"))
                .stockQuantity(50)
                .active(true)
                .build();
        productRepository.save(existingProduct);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testProductRequest)))
                .andExpect(status().isConflict());
    }

    @Test
    void createProduct_InvalidData_Returns400() throws Exception {
        ProductRequest invalidRequest = ProductRequest.builder()
                .sku("")
                .name("AB")
                .price(new BigDecimal("-10.00"))
                .stockQuantity(-5)
                .build();

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getProductById_Success() throws Exception {
        Product savedProduct = productRepository.save(Product.builder()
                .sku("INT-TEST-002")
                .name("Test Product 2")
                .price(new BigDecimal("49.99"))
                .stockQuantity(50)
                .active(true)
                .build());

        mockMvc.perform(get("/api/products/" + savedProduct.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedProduct.getId()))
                .andExpect(jsonPath("$.sku").value("INT-TEST-002"))
                .andExpect(jsonPath("$.name").value("Test Product 2"));
    }

    @Test
    void getProductById_NotFound_Returns404() throws Exception {
        mockMvc.perform(get("/api/products/9999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getProductBySku_Success() throws Exception {
        productRepository.save(Product.builder()
                .sku("INT-TEST-003")
                .name("Test Product 3")
                .price(new BigDecimal("29.99"))
                .stockQuantity(30)
                .active(true)
                .build());

        mockMvc.perform(get("/api/products/sku/INT-TEST-003"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sku").value("INT-TEST-003"))
                .andExpect(jsonPath("$.name").value("Test Product 3"));
    }

    @Test
    void getAllProducts_Success() throws Exception {
        productRepository.save(Product.builder()
                .sku("INT-TEST-004")
                .name("Test Product 4")
                .price(new BigDecimal("19.99"))
                .stockQuantity(20)
                .active(true)
                .build());

        productRepository.save(Product.builder()
                .sku("INT-TEST-005")
                .name("Test Product 5")
                .price(new BigDecimal("39.99"))
                .stockQuantity(40)
                .active(true)
                .build());

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getAllProducts_FilterByCategory_Success() throws Exception {
        productRepository.save(Product.builder()
                .sku("INT-TEST-006")
                .name("Electronics Product")
                .price(new BigDecimal("99.99"))
                .stockQuantity(10)
                .category("Electronics")
                .active(true)
                .build());

        productRepository.save(Product.builder()
                .sku("INT-TEST-007")
                .name("Books Product")
                .price(new BigDecimal("19.99"))
                .stockQuantity(20)
                .category("Books")
                .active(true)
                .build());

        mockMvc.perform(get("/api/products?category=Electronics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].category").value("Electronics"));
    }

    @Test
    void getAllProducts_FilterByActiveOnly_Success() throws Exception {
        productRepository.save(Product.builder()
                .sku("INT-TEST-008")
                .name("Active Product")
                .price(new BigDecimal("49.99"))
                .stockQuantity(50)
                .active(true)
                .build());

        productRepository.save(Product.builder()
                .sku("INT-TEST-009")
                .name("Inactive Product")
                .price(new BigDecimal("29.99"))
                .stockQuantity(30)
                .active(false)
                .build());

        mockMvc.perform(get("/api/products?activeOnly=true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].active").value(true));
    }

    @Test
    void getAllProducts_SearchByKeyword_Success() throws Exception {
        productRepository.save(Product.builder()
                .sku("INT-TEST-010")
                .name("Laptop Computer")
                .description("High performance laptop")
                .price(new BigDecimal("999.99"))
                .stockQuantity(10)
                .active(true)
                .build());

        productRepository.save(Product.builder()
                .sku("INT-TEST-011")
                .name("Desktop Computer")
                .description("Gaming desktop")
                .price(new BigDecimal("1299.99"))
                .stockQuantity(5)
                .active(true)
                .build());

        mockMvc.perform(get("/api/products?search=Computer"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void updateProduct_Success() throws Exception {
        Product savedProduct = productRepository.save(Product.builder()
                .sku("INT-TEST-012")
                .name("Original Product")
                .price(new BigDecimal("99.99"))
                .stockQuantity(100)
                .active(true)
                .build());

        ProductRequest updateRequest = ProductRequest.builder()
                .sku("INT-TEST-012")
                .name("Updated Product")
                .description("Updated Description")
                .price(new BigDecimal("149.99"))
                .stockQuantity(150)
                .category("Electronics")
                .active(true)
                .build();

        mockMvc.perform(put("/api/products/" + savedProduct.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Product"))
                .andExpect(jsonPath("$.price").value(149.99))
                .andExpect(jsonPath("$.stockQuantity").value(150));
    }

    @Test
    void updateProduct_NotFound_Returns404() throws Exception {
        mockMvc.perform(put("/api/products/9999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testProductRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteProduct_Success() throws Exception {
        Product savedProduct = productRepository.save(Product.builder()
                .sku("INT-TEST-013")
                .name("Product to Delete")
                .price(new BigDecimal("49.99"))
                .stockQuantity(50)
                .active(true)
                .build());

        mockMvc.perform(delete("/api/products/" + savedProduct.getId()))
                .andExpect(status().isNoContent());

        assertThat(productRepository.findById(savedProduct.getId())).isEmpty();
    }

    @Test
    void deleteProduct_NotFound_Returns404() throws Exception {
        mockMvc.perform(delete("/api/products/9999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateStock_Success() throws Exception {
        Product savedProduct = productRepository.save(Product.builder()
                .sku("INT-TEST-014")
                .name("Stock Update Product")
                .price(new BigDecimal("59.99"))
                .stockQuantity(100)
                .active(true)
                .build());

        mockMvc.perform(patch("/api/products/" + savedProduct.getId() + "/stock?quantity=200"))
                .andExpect(status().isOk());

        Product updatedProduct = productRepository.findById(savedProduct.getId()).orElseThrow();
        assertThat(updatedProduct.getStockQuantity()).isEqualTo(200);
    }

    @Test
    void updateStock_NotFound_Returns404() throws Exception {
        mockMvc.perform(patch("/api/products/9999/stock?quantity=200"))
                .andExpect(status().isNotFound());
    }
}