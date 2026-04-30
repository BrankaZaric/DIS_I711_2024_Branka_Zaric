package com.ecommerce.order;

import com.ecommerce.order.client.ProductClient;
import com.ecommerce.order.dto.*;
import com.ecommerce.order.exception.InsufficientStockException;
import com.ecommerce.order.exception.OrderNotFoundException;
import com.ecommerce.order.exception.ProductNotAvailableException;
import com.ecommerce.order.model.Order;
import com.ecommerce.order.model.OrderStatus;
import com.ecommerce.order.repository.OrderRepository;
import com.ecommerce.order.service.OrderService;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
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

    private ProductResponse mockProduct;
    private OrderRequest orderRequest;

    @BeforeEach
    void setUp() {
        mockProduct = ProductResponse.builder()
                .id(1L)
                .sku("LAPTOP-001")
                .name("Gaming Laptop")
                .price(new BigDecimal("1299.99"))
                .stockQuantity(50)
                .active(true)
                .build();

        OrderItemRequest itemRequest = OrderItemRequest.builder()
                .productSku("LAPTOP-001")
                .quantity(2)
                .build();

        orderRequest = OrderRequest.builder()
                .customerEmail("test@example.com")
                .customerName("Test User")
                .shippingAddress("123 Test St")
                .items(Arrays.asList(itemRequest))
                .build();
    }

    @Test
    void createOrder_Success() {
        when(productClient.getProductBySku("LAPTOP-001")).thenReturn(mockProduct);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(1L);
            return order;
        });
        when(orderRepository.existsByOrderNumber(anyString())).thenReturn(false);

        OrderResponse response = orderService.createOrder(orderRequest);

        assertNotNull(response);
        assertEquals("test@example.com", response.getCustomerEmail());
        assertEquals("Test User", response.getCustomerName());
        assertEquals(1, response.getItems().size());
        assertEquals(new BigDecimal("2599.98"), response.getTotalAmount());
        assertEquals(OrderStatus.PENDING, response.getStatus());

        verify(productClient, times(1)).getProductBySku("LAPTOP-001");
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void createOrder_ProductNotFound() {
        when(productClient.getProductBySku("LAPTOP-001"))
                .thenThrow(mock(FeignException.NotFound.class));

        assertThrows(ProductNotAvailableException.class, () -> orderService.createOrder(orderRequest));

        verify(productClient, times(1)).getProductBySku("LAPTOP-001");
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void createOrder_InsufficientStock() {
        mockProduct.setStockQuantity(1);
        when(productClient.getProductBySku("LAPTOP-001")).thenReturn(mockProduct);

        assertThrows(InsufficientStockException.class, () -> orderService.createOrder(orderRequest));

        verify(productClient, times(1)).getProductBySku("LAPTOP-001");
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void createOrder_ProductNotActive() {
        mockProduct.setActive(false);
        when(productClient.getProductBySku("LAPTOP-001")).thenReturn(mockProduct);

        assertThrows(ProductNotAvailableException.class, () -> orderService.createOrder(orderRequest));

        verify(productClient, times(1)).getProductBySku("LAPTOP-001");
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void getOrderById_Success() {
        Order order = createMockOrder();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        OrderResponse response = orderService.getOrderById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("ORD-12345678", response.getOrderNumber());

        verify(orderRepository, times(1)).findById(1L);
    }

    @Test
    void getOrderById_NotFound() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> orderService.getOrderById(1L));

        verify(orderRepository, times(1)).findById(1L);
    }

    @Test
    void getOrderByOrderNumber_Success() {
        Order order = createMockOrder();
        when(orderRepository.findByOrderNumber("ORD-12345678")).thenReturn(Optional.of(order));

        OrderResponse response = orderService.getOrderByOrderNumber("ORD-12345678");

        assertNotNull(response);
        assertEquals("ORD-12345678", response.getOrderNumber());

        verify(orderRepository, times(1)).findByOrderNumber("ORD-12345678");
    }

    @Test
    void getAllOrders_Success() {
        List<Order> orders = Arrays.asList(createMockOrder(), createMockOrder());
        when(orderRepository.findAll()).thenReturn(orders);

        List<OrderResponse> responses = orderService.getAllOrders();

        assertNotNull(responses);
        assertEquals(2, responses.size());

        verify(orderRepository, times(1)).findAll();
    }

    @Test
    void getOrdersByCustomerEmail_Success() {
        List<Order> orders = Arrays.asList(createMockOrder());
        when(orderRepository.findByCustomerEmailOrderByCreatedAtDesc("test@example.com"))
                .thenReturn(orders);

        List<OrderResponse> responses = orderService.getOrdersByCustomerEmail("test@example.com");

        assertNotNull(responses);
        assertEquals(1, responses.size());

        verify(orderRepository, times(1))
                .findByCustomerEmailOrderByCreatedAtDesc("test@example.com");
    }

    @Test
    void getOrdersByStatus_Success() {
        List<Order> orders = Arrays.asList(createMockOrder());
        when(orderRepository.findByStatusOrderByCreatedAtDesc(OrderStatus.PENDING))
                .thenReturn(orders);

        List<OrderResponse> responses = orderService.getOrdersByStatus(OrderStatus.PENDING);

        assertNotNull(responses);
        assertEquals(1, responses.size());

        verify(orderRepository, times(1))
                .findByStatusOrderByCreatedAtDesc(OrderStatus.PENDING);
    }

    @Test
    void updateOrderStatus_Success() {
        Order order = createMockOrder();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        OrderResponse response = orderService.updateOrderStatus(1L, OrderStatus.CONFIRMED);

        assertNotNull(response);
        assertEquals(OrderStatus.CONFIRMED, response.getStatus());

        verify(orderRepository, times(1)).findById(1L);
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    void cancelOrder_Success() {
        Order order = createMockOrder();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        orderService.cancelOrder(1L);

        assertEquals(OrderStatus.CANCELLED, order.getStatus());

        verify(orderRepository, times(1)).findById(1L);
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    void cancelOrder_DeliveredOrder_ThrowsException() {
        Order order = createMockOrder();
        order.setStatus(OrderStatus.DELIVERED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(IllegalStateException.class, () -> orderService.cancelOrder(1L));

        verify(orderRepository, times(1)).findById(1L);
        verify(orderRepository, never()).save(any(Order.class));
    }

    private Order createMockOrder() {
        Order order = Order.builder()
                .id(1L)
                .orderNumber("ORD-12345678")
                .customerEmail("test@example.com")
                .customerName("Test User")
                .shippingAddress("123 Test St")
                .totalAmount(new BigDecimal("1299.99"))
                .status(OrderStatus.PENDING)
                .build();

        return order;
    }
}