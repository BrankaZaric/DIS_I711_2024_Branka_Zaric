package com.ecommerce.order.model;

public enum OrderStatus {
    PENDING,        // Order created, waiting for payment
    CONFIRMED,      // Payment confirmed
    PROCESSING,     // Order being prepared
    SHIPPED,        // Order shipped
    DELIVERED,      // Order delivered
    CANCELLED       // Order cancelled
}