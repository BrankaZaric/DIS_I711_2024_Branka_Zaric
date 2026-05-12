package com.ecommerce.order.messaging;

import com.ecommerce.order.config.RabbitMQConfig;
import com.ecommerce.order.dto.OrderEventMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishOrderEvent(OrderEventMessage message) {
        log.info("Publishing order event for order: {}", message.getOrderNumber());

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.ORDER_EXCHANGE,
                RabbitMQConfig.ORDER_ROUTING_KEY,
                message
        );

        log.info("Order event published successfully for order: {}", message.getOrderNumber());
    }
}
