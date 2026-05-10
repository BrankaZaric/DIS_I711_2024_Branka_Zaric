package com.ecommerce.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderEventMessage {
    private String orderNumber;
    private String customerEmail;
    private String customerName;
    private String status;
    private Double totalAmount;
}