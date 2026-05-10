package com.ecommerce.notification.messaging;

import com.ecommerce.notification.dto.OrderEventMessage;
import com.ecommerce.notification.model.Notification;
import com.ecommerce.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventConsumer {

    private final NotificationService notificationService;

    @RabbitListener(queues = "order.notification.queue")
    public void handleOrderEvent(OrderEventMessage message) {
        log.info("Received order event for order: {}", message.getOrderNumber());

        String subject = "Order Update: " + message.getOrderNumber();
        String messageBody = buildNotificationMessage(message);

        // Send email notification
        notificationService.sendNotification(
                Notification.NotificationType.EMAIL,
                message.getCustomerEmail(),
                subject,
                messageBody,
                message.getOrderNumber()
        );

        log.info("Notification processed for order: {}", message.getOrderNumber());
    }

    private String buildNotificationMessage(OrderEventMessage message) {
        return String.format(
                "Dear %s,\n\n" +
                "Your order %s has been %s.\n" +
                "Total Amount: $%.2f\n\n" +
                "Thank you for your purchase!",
                message.getCustomerName(),
                message.getOrderNumber(),
                message.getStatus().toLowerCase(),
                message.getTotalAmount()
        );
    }
}