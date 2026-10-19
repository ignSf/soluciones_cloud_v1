package com.example.demo.messaging.lavinmq;

import com.example.demo.config.LavinMqConfig;
import com.example.demo.model.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Productor para Bus 3: LavinMQ
 * Publica eventos en el exchange 'shipping.exchange' para que shipping-service
 * genere automáticamente la guía y el código de seguimiento logístico.
 */
@Component
public class LavinMqShippingPublisher {

    private static final Logger log = LoggerFactory.getLogger(LavinMqShippingPublisher.class);

    private final RabbitTemplate lavinMqRabbitTemplate;
    private final boolean enabled;

    public LavinMqShippingPublisher(
            @Qualifier("lavinMqRabbitTemplate") RabbitTemplate lavinMqRabbitTemplate,
            @Value("${app.lavinmq.shipping.enabled:true}") boolean enabled) {
        this.lavinMqRabbitTemplate = lavinMqRabbitTemplate;
        this.enabled = enabled;
    }

    public void publicarDespachoPedido(Order order) {
        if (!enabled) {
            log.info("LavinMQ desactivado: no se publica evento de despacho para pedido {}", order.getId());
            return;
        }

        ShippingOrderEvent evento = new ShippingOrderEvent(
                order.getId(),
                order.getUserEmail(),
                order.getProductId(),
                order.getProductName(),
                order.getQuantity(),
                order.getTotalAmount()
        );

        try {
            lavinMqRabbitTemplate.convertAndSend(
                    LavinMqConfig.SHIPPING_EXCHANGE,
                    LavinMqConfig.SHIPPING_CREATED_KEY,
                    evento
            );
            log.info("LavinMQ PUBLICADO: evento despacho pedidoId={}", order.getId());
        } catch (AmqpException e) {
            // Regla de resiliencia: si LavinMQ no responde, el pedido NO se cae; solo se registra el error
            log.error("LavinMQ ERROR al publicar despacho pedidoId={}: {}", order.getId(), e.getMessage());
        }
    }
}
