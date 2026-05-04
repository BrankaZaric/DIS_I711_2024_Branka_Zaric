package com.ecommerce.order.service;

import com.ecommerce.order.client.ProductClient;
import com.ecommerce.order.dto.*;
import com.ecommerce.order.exception.InsufficientStockException;
import com.ecommerce.order.exception.OrderNotFoundException;
import com.ecommerce.order.exception.ProductNotAvailableException;
import com.ecommerce.order.model.Order;
import com.ecommerce.order.model.OrderStatus;
import com.ecommerce.order.repository.OrderRepository;
import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductClient productClient;

    @InjectMocks
    private OrderService orderService;

    private Order testOrder;
    private OrderRequest testOrderRequest;
    private ProductResponse testProduct;

    @BeforeEach
    void setUp() {
        testProduct = ProductResponse.builder()
                .id(1L)
                .sku("PROD-001")
                .name("Test Product")
                .price(new BigDecimal("99.99"))
                .stockQuantity(100)
                .active(true)
                .build();

        OrderItemRequest itemRequest = OrderItemRequest.builder()
                .productSku("PROD-001")
                .quantity(2)
                .build();

        testOrderRequest = OrderRequest.builder()
                .customerEmail("test@example.com")
                .customerName("Test Customer")
                .shippingAddress("123 Test Street")
                .items(Collections.singletonList(itemRequest))
                .build();

        testOrder = Order.builder()
                .id(1L)
                .orderNumber("ORD-12345678")
                .customerEmail("test@example.com")
                .customerName("Test Customer")
                .shippingAddress("123 Test Street")
                .totalAmount(new BigDecimal("199.98"))
                .status(OrderStatus.PENDING)
                .orderItems(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void createOrder_Success() {
        when(productClient.getProductBySku("PROD-001")).thenReturn(testProduct);
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        when(orderRepository.existsByOrderNumber(anyString())).thenReturn(false);

        OrderResponse response = orderService.createOrder(testOrderRequest);

        assertThat(response).isNotNull();
        assertThat(response.getCustomerEmail()).isEqualTo("test@example.com");
        assertThat(response.getCustomerName()).isEqualTo("Test Customer");
        assertThat(response.getStatus()).isEqualTo(OrderStatus.PENDING);

        verify(productClient).getProductBySku("PROD-001");
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void createOrder_ProductNotFound_ThrowsException() {
        Request request = Request.create(Request.HttpMethod.GET, "/test",
                Collections.emptyMap(), null, new RequestTemplate());

        when(productClient.getProductBySku("PROD-001"))
                .thenThrow(new FeignException.NotFound("Not found", request, null, null));

        assertThatThrownBy(() -> orderService.createOrder(testOrderRequest))
                .isInstanceOf(ProductNotAvailableException.class)
                .hasMessageContaining("Product not found with SKU");

        verify(productClient).getProductBySku("PROD-001");
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void createOrder_ProductNotActive_ThrowsException() {
        ProductResponse inactiveProduct = ProductResponse.builder()
                .id(1L)
                .sku("PROD-001")
                .name("Inactive Product")
                .price(new BigDecimal("99.99"))
                .stockQuantity(100)
                .active(false)
                .build();

        when(productClient.getProductBySku("PROD-001")).thenReturn(inactiveProduct);

        assertThatThrownBy(() -> orderService.createOrder(testOrderRequest))
                .isInstanceOf(ProductNotAvailableException.class)
                .hasMessageContaining("Product is not active");

        verify(productClient).getProductBySku("PROD-001");
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void createOrder_InsufficientStock_ThrowsException() {
        ProductResponse lowStockProduct = ProductResponse.builder()
                .id(1L)
                .sku("PROD-001")
                .name("Low Stock Product")
                .price(new BigDecimal("99.99"))
                .stockQuantity(1)
                .active(true)
                .build();

        when(productClient.getProductBySku("PROD-001")).thenReturn(lowStockProduct);

        assertThatThrownBy(() -> orderService.createOrder(testOrderRequest))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("Insufficient stock");

        verify(productClient).getProductBySku("PROD-001");
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void getOrderById_Success() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        OrderResponse response = orderService.getOrderById(1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getOrderNumber()).isEqualTo("ORD-12345678");

        verify(orderRepository).findById(1L);
    }

    @Test
    void getOrderById_NotFound_ThrowsException() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrderById(1L))
                .isInstanceOf(OrderNotFoundException.class);

        verify(orderRepository).findById(1L);
    }

    @Test
    void getOrderByOrderNumber_Success() {
        when(orderRepository.findByOrderNumber("ORD-12345678")).thenReturn(Optional.of(testOrder));

        OrderResponse response = orderService.getOrderByOrderNumber("ORD-12345678");

        assertThat(response).isNotNull();
        assertThat(response.getOrderNumber()).isEqualTo("ORD-12345678");

        verify(orderRepository).findByOrderNumber("ORD-12345678");
    }

    @Test
    void getOrderByOrderNumber_NotFound_ThrowsException() {
        when(orderRepository.findByOrderNumber("ORD-12345678")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrderByOrderNumber("ORD-12345678"))
                .isInstanceOf(OrderNotFoundException.class);

        verify(orderRepository).findByOrderNumber("ORD-12345678");
    }

    @Test
    void getAllOrders_Success() {
        Order order2 = Order.builder()
                .id(2L)
                .orderNumber("ORD-87654321")
                .customerEmail("test2@example.com")
                .totalAmount(new BigDecimal("299.98"))
                .status(OrderStatus.CONFIRMED)
                .orderItems(new ArrayList<>())
                .build();

        when(orderRepository.findAll()).thenReturn(Arrays.asList(testOrder, order2));

        List<OrderResponse> orders = orderService.getAllOrders();

        assertThat(orders).hasSize(2);
        assertThat(orders.get(0).getOrderNumber()).isEqualTo("ORD-12345678");
        assertThat(orders.get(1).getOrderNumber()).isEqualTo("ORD-87654321");

        verify(orderRepository).findAll();
    }

    @Test
    void getOrdersByCustomerEmail_Success() {
        when(orderRepository.findByCustomerEmailOrderByCreatedAtDesc("test@example.com"))
                .thenReturn(Collections.singletonList(testOrder));

        List<OrderResponse> orders = orderService.getOrdersByCustomerEmail("test@example.com");

        assertThat(orders).hasSize(1);
        assertThat(orders.get(0).getCustomerEmail()).isEqualTo("test@example.com");

        verify(orderRepository).findByCustomerEmailOrderByCreatedAtDesc("test@example.com");
    }

    @Test
    void getOrdersByStatus_Success() {
        when(orderRepository.findByStatusOrderByCreatedAtDesc(OrderStatus.PENDING))
                .thenReturn(Collections.singletonList(testOrder));

        List<OrderResponse> orders = orderService.getOrdersByStatus(OrderStatus.PENDING);

        assertThat(orders).hasSize(1);
        assertThat(orders.get(0).getStatus()).isEqualTo(OrderStatus.PENDING);

        verify(orderRepository).findByStatusOrderByCreatedAtDesc(OrderStatus.PENDING);
    }

    @Test
    void updateOrderStatus_Success() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        OrderResponse response = orderService.updateOrderStatus(1L, OrderStatus.CONFIRMED);

        assertThat(response).isNotNull();
        verify(orderRepository).findById(1L);
        verify(orderRepository).save(testOrder);
    }

    @Test
    void updateOrderStatus_NotFound_ThrowsException() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.updateOrderStatus(1L, OrderStatus.CONFIRMED))
                .isInstanceOf(OrderNotFoundException.class);

        verify(orderRepository).findById(1L);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void cancelOrder_Success() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        orderService.cancelOrder(1L);

        verify(orderRepository).findById(1L);
        verify(orderRepository).save(testOrder);
        assertThat(testOrder.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    void cancelOrder_DeliveredOrder_ThrowsException() {
        testOrder.setStatus(OrderStatus.DELIVERED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        assertThatThrownBy(() -> orderService.cancelOrder(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot cancel delivered order");

        verify(orderRepository).findById(1L);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void cancelOrder_NotFound_ThrowsException() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.cancelOrder(1L))
                .isInstanceOf(OrderNotFoundException.class);

        verify(orderRepository).findById(1L);
        verify(orderRepository, never()).save(any(Order.class));
    }
}