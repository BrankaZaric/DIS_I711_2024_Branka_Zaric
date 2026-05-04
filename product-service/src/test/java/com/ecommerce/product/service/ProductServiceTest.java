package com.ecommerce.product.service;

import com.ecommerce.product.dto.ProductRequest;
import com.ecommerce.product.dto.ProductResponse;
import com.ecommerce.product.exception.DuplicateProductException;
import com.ecommerce.product.exception.ProductNotFoundException;
import com.ecommerce.product.model.Product;
import com.ecommerce.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product testProduct;
    private ProductRequest testProductRequest;

    @BeforeEach
    void setUp() {
        testProduct = Product.builder()
                .id(1L)
                .sku("TEST-001")
                .name("Test Product")
                .description("Test Description")
                .price(new BigDecimal("99.99"))
                .stockQuantity(100)
                .category("Electronics")
                .imageUrl("http://test.com/image.jpg")
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testProductRequest = ProductRequest.builder()
                .sku("TEST-001")
                .name("Test Product")
                .description("Test Description")
                .price(new BigDecimal("99.99"))
                .stockQuantity(100)
                .category("Electronics")
                .imageUrl("http://test.com/image.jpg")
                .active(true)
                .build();
    }

    @Test
    void createProduct_Success() {
        when(productRepository.existsBySku(anyString())).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        ProductResponse response = productService.createProduct(testProductRequest);

        assertThat(response).isNotNull();
        assertThat(response.getSku()).isEqualTo("TEST-001");
        assertThat(response.getName()).isEqualTo("Test Product");
        assertThat(response.getPrice()).isEqualTo(new BigDecimal("99.99"));

        verify(productRepository).existsBySku("TEST-001");
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void createProduct_DuplicateSku_ThrowsException() {
        when(productRepository.existsBySku(anyString())).thenReturn(true);

        assertThatThrownBy(() -> productService.createProduct(testProductRequest))
                .isInstanceOf(DuplicateProductException.class);

        verify(productRepository).existsBySku("TEST-001");
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void getProductById_Success() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        ProductResponse response = productService.getProductById(1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Test Product");

        verify(productRepository).findById(1L);
    }

    @Test
    void getProductById_NotFound_ThrowsException() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById(1L))
                .isInstanceOf(ProductNotFoundException.class);

        verify(productRepository).findById(1L);
    }

    @Test
    void getProductBySku_Success() {
        when(productRepository.findBySku("TEST-001")).thenReturn(Optional.of(testProduct));

        ProductResponse response = productService.getProductBySku("TEST-001");

        assertThat(response).isNotNull();
        assertThat(response.getSku()).isEqualTo("TEST-001");

        verify(productRepository).findBySku("TEST-001");
    }

    @Test
    void getProductBySku_NotFound_ThrowsException() {
        when(productRepository.findBySku("TEST-001")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductBySku("TEST-001"))
                .isInstanceOf(ProductNotFoundException.class);

        verify(productRepository).findBySku("TEST-001");
    }

    @Test
    void getAllProducts_Success() {
        Product product2 = Product.builder()
                .id(2L)
                .sku("TEST-002")
                .name("Test Product 2")
                .price(new BigDecimal("49.99"))
                .stockQuantity(50)
                .active(true)
                .build();

        when(productRepository.findAll()).thenReturn(Arrays.asList(testProduct, product2));

        List<ProductResponse> products = productService.getAllProducts();

        assertThat(products).hasSize(2);
        assertThat(products.get(0).getSku()).isEqualTo("TEST-001");
        assertThat(products.get(1).getSku()).isEqualTo("TEST-002");

        verify(productRepository).findAll();
    }

    @Test
    void getActiveProducts_Success() {
        when(productRepository.findByActiveTrue()).thenReturn(Arrays.asList(testProduct));

        List<ProductResponse> products = productService.getActiveProducts();

        assertThat(products).hasSize(1);
        assertThat(products.get(0).getActive()).isTrue();

        verify(productRepository).findByActiveTrue();
    }

    @Test
    void getProductsByCategory_Success() {
        when(productRepository.findByCategory("Electronics"))
                .thenReturn(Arrays.asList(testProduct));

        List<ProductResponse> products = productService.getProductsByCategory("Electronics");

        assertThat(products).hasSize(1);
        assertThat(products.get(0).getCategory()).isEqualTo("Electronics");

        verify(productRepository).findByCategory("Electronics");
    }

    @Test
    void searchProducts_Success() {
        when(productRepository.searchProducts("Test"))
                .thenReturn(Arrays.asList(testProduct));

        List<ProductResponse> products = productService.searchProducts("Test");

        assertThat(products).hasSize(1);
        assertThat(products.get(0).getName()).contains("Test");

        verify(productRepository).searchProducts("Test");
    }

    @Test
    void updateProduct_Success() {
        ProductRequest updateRequest = ProductRequest.builder()
                .sku("TEST-001")
                .name("Updated Product")
                .description("Updated Description")
                .price(new BigDecimal("149.99"))
                .stockQuantity(150)
                .category("Electronics")
                .imageUrl("http://test.com/new-image.jpg")
                .active(true)
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        ProductResponse response = productService.updateProduct(1L, updateRequest);

        assertThat(response).isNotNull();
        verify(productRepository).findById(1L);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void updateProduct_NotFound_ThrowsException() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.updateProduct(1L, testProductRequest))
                .isInstanceOf(ProductNotFoundException.class);

        verify(productRepository).findById(1L);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void updateProduct_DuplicateSku_ThrowsException() {
        ProductRequest updateRequest = ProductRequest.builder()
                .sku("TEST-002")
                .name("Updated Product")
                .price(new BigDecimal("149.99"))
                .stockQuantity(150)
                .active(true)
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.existsBySku("TEST-002")).thenReturn(true);

        assertThatThrownBy(() -> productService.updateProduct(1L, updateRequest))
                .isInstanceOf(DuplicateProductException.class);

        verify(productRepository).findById(1L);
        verify(productRepository).existsBySku("TEST-002");
    }

    @Test
    void deleteProduct_Success() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        doNothing().when(productRepository).delete(any(Product.class));

        productService.deleteProduct(1L);

        verify(productRepository).findById(1L);
        verify(productRepository).delete(testProduct);
    }

    @Test
    void deleteProduct_NotFound_ThrowsException() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.deleteProduct(1L))
                .isInstanceOf(ProductNotFoundException.class);

        verify(productRepository).findById(1L);
        verify(productRepository, never()).delete(any(Product.class));
    }

    @Test
    void updateStock_Success() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        productService.updateStock(1L, 200);

        verify(productRepository).findById(1L);
        verify(productRepository).save(testProduct);
        assertThat(testProduct.getStockQuantity()).isEqualTo(200);
    }

    @Test
    void updateStock_NotFound_ThrowsException() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.updateStock(1L, 200))
                .isInstanceOf(ProductNotFoundException.class);

        verify(productRepository).findById(1L);
        verify(productRepository, never()).save(any(Product.class));
    }
}