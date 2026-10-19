package com.example.demo.messaging;

import com.example.demo.config.RabbitConfig;
import com.example.demo.model.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishOrderCreated(Order order) {
        if (order.getProductId() == null) {
            log.warn("Pedido {} sin productId: no se publica evento de inventario", order.getId());
            return;
        }

        OrderCreatedEvent event = new OrderCreatedEvent(
                order.getId(),
                order.getUserEmail(),
                order.getProductId(),
                order.getQuantity(),
                order.getTotalAmount());

        try {
            rabbitTemplate.convertAndSend(
                    RabbitConfig.ORDERS_EXCHANGE,
                    RabbitConfig.ORDER_CREATED_KEY,
                    event);
            log.info("Evento OrderCreated publicado: pedido {}", order.getId());
        } catch (AmqpException e) {
            // El pedido ya está guardado: si RabbitMQ no responde, se registra el error y se sigue
            log.error("No se pudo publicar el evento del pedido {}: {}", order.getId(), e.getMessage());
        }
    }
}
