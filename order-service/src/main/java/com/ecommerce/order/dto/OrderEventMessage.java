package com.ecommerce.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderEventMessage {
    private String orderNumber;
    private String customerEmail;
    private String customerName;
    private String status;
    private Double totalAmount;
}
