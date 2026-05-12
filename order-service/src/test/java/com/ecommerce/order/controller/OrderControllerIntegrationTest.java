package com.ecommerce.order.controller;

import com.ecommerce.order.client.ProductClient;
import com.ecommerce.order.dto.OrderItemRequest;
import com.ecommerce.order.dto.OrderRequest;
import com.ecommerce.order.dto.ProductResponse;
import com.ecommerce.order.messaging.OrderEventPublisher;
import com.ecommerce.order.model.Order;
import com.ecommerce.order.model.OrderStatus;
import com.ecommerce.order.repository.OrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OrderControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrderRepository orderRepository;

    @MockBean
    private ProductClient productClient;

    @MockBean
    private OrderEventPublisher orderEventPublisher;

    private OrderRequest testOrderRequest;
    private ProductResponse testProduct;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();

        testProduct = ProductResponse.builder()
                .id(1L)
                .sku("PROD-INT-001")
                .name("Integration Test Product")
                .price(new BigDecimal("99.99"))
                .stockQuantity(100)
                .active(true)
                .build();

        OrderItemRequest itemRequest = OrderItemRequest.builder()
                .productSku("PROD-INT-001")
                .quantity(2)
                .build();

        testOrderRequest = OrderRequest.builder()
                .customerEmail("test@example.com")
                .customerName("Test Customer")
                .shippingAddress("123 Test Street, Test City")
                .items(Collections.singletonList(itemRequest))
                .build();
    }

    @AfterEach
    void tearDown() {
        orderRepository.deleteAll();
    }

    @Test
    void createOrder_Success() throws Exception {
        when(productClient.getProductBySku("PROD-INT-001")).thenReturn(testProduct);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testOrderRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customerEmail").value("test@example.com"))
                .andExpect(jsonPath("$.customerName").value("Test Customer"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.orderNumber").exists())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items[0].productSku").value("PROD-INT-001"))
                .andExpect(jsonPath("$.items[0].quantity").value(2));

        assertThat(orderRepository.count()).isEqualTo(1);
    }

    @Test
    void createOrder_InvalidData_Returns400() throws Exception {
        OrderRequest invalidRequest = OrderRequest.builder()
                .customerEmail("invalid-email")
                .customerName("")
                .items(Collections.emptyList())
                .build();

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createOrder_InsufficientStock_Returns400() throws Exception {
        ProductResponse lowStockProduct = ProductResponse.builder()
                .id(1L)
                .sku("PROD-INT-001")
                .name("Low Stock Product")
                .price(new BigDecimal("99.99"))
                .stockQuantity(1)
                .active(true)
                .build();

        when(productClient.getProductBySku("PROD-INT-001")).thenReturn(lowStockProduct);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testOrderRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Insufficient stock")));
    }

    @Test
    void getOrderById_Success() throws Exception {
        when(productClient.getProductBySku(anyString())).thenReturn(testProduct);

        String createResponse = mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testOrderRequest)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long orderId = objectMapper.readTree(createResponse).get("id").asLong();

        mockMvc.perform(get("/api/orders/" + orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId))
                .andExpect(jsonPath("$.customerEmail").value("test@example.com"));
    }

    @Test
    void getOrderById_NotFound_Returns404() throws Exception {
        mockMvc.perform(get("/api/orders/9999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getOrderByOrderNumber_Success() throws Exception {
        when(productClient.getProductBySku(anyString())).thenReturn(testProduct);

        String createResponse = mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testOrderRequest)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String orderNumber = objectMapper.readTree(createResponse).get("orderNumber").asText();

        mockMvc.perform(get("/api/orders/number/" + orderNumber))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderNumber").value(orderNumber));
    }

    @Test
    void getAllOrders_Success() throws Exception {
        when(productClient.getProductBySku(anyString())).thenReturn(testProduct);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testOrderRequest)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testOrderRequest)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getAllOrders_FilterByCustomerEmail_Success() throws Exception {
        when(productClient.getProductBySku(anyString())).thenReturn(testProduct);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testOrderRequest)))
                .andExpect(status().isCreated());

        OrderRequest anotherRequest = OrderRequest.builder()
                .customerEmail("another@example.com")
                .customerName("Another Customer")
                .shippingAddress("456 Another Street")
                .items(Collections.singletonList(OrderItemRequest.builder()
                        .productSku("PROD-INT-001")
                        .quantity(1)
                        .build()))
                .build();

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(anotherRequest)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/orders?customerEmail=test@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].customerEmail").value("test@example.com"));
    }

    @Test
    void getAllOrders_FilterByStatus_Success() throws Exception {
        when(productClient.getProductBySku(anyString())).thenReturn(testProduct);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testOrderRequest)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/orders?status=PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    void updateOrderStatus_Success() throws Exception {
        when(productClient.getProductBySku(anyString())).thenReturn(testProduct);

        String createResponse = mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testOrderRequest)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long orderId = objectMapper.readTree(createResponse).get("id").asLong();

        mockMvc.perform(patch("/api/orders/" + orderId + "/status?status=CONFIRMED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));

        Order updatedOrder = orderRepository.findById(orderId).orElseThrow();
        assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
    }

    @Test
    void updateOrderStatus_NotFound_Returns404() throws Exception {
        mockMvc.perform(patch("/api/orders/9999/status?status=CONFIRMED"))
                .andExpect(status().isNotFound());
    }

    @Test
    void cancelOrder_Success() throws Exception {
        when(productClient.getProductBySku(anyString())).thenReturn(testProduct);

        String createResponse = mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testOrderRequest)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long orderId = objectMapper.readTree(createResponse).get("id").asLong();

        mockMvc.perform(delete("/api/orders/" + orderId))
                .andExpect(status().isNoContent());

        Order cancelledOrder = orderRepository.findById(orderId).orElseThrow();
        assertThat(cancelledOrder.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    void cancelOrder_NotFound_Returns404() throws Exception {
        mockMvc.perform(delete("/api/orders/9999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void cancelOrder_DeliveredOrder_Returns400() throws Exception {
        when(productClient.getProductBySku(anyString())).thenReturn(testProduct);

        String createResponse = mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testOrderRequest)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long orderId = objectMapper.readTree(createResponse).get("id").asLong();

        Order order = orderRepository.findById(orderId).orElseThrow();
        order.setStatus(OrderStatus.DELIVERED);
        orderRepository.save(order);

        mockMvc.perform(delete("/api/orders/" + orderId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Cannot cancel delivered order")));
    }
}