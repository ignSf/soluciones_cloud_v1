package com.example.shipping.messaging;

import com.example.shipping.config.LavinMqConfig;
import com.example.shipping.model.Shipment;
import com.example.shipping.service.ShippingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consumidor LavinMQ (AMQP 0-9-1)
 * Escucha eventos en la cola 'shipping.order-created.queue'.
 * Si el procesamiento es exitoso, persiste el envío con número de seguimiento.
 * Si ocurre un error irrecuperable, envía a la Dead Letter Queue de LavinMQ.
 */
@Component
public class ShippingConsumer {

    private static final Logger log = LoggerFactory.getLogger(ShippingConsumer.class);

    private final ShippingService shippingService;

    public ShippingConsumer(ShippingService shippingService) {
        this.shippingService = shippingService;
    }

    @RabbitListener(queues = LavinMqConfig.SHIPPING_QUEUE)
    public void handleOrderCreated(ShippingOrderEvent event) {
        log.info("LavinMQ RECIBIDO: procesando despacho de pedido {} -> producto: {}",
                event.orderId(), event.productName());
        try {
            Shipment shipment = shippingService.processOrderShipping(event);
            log.info("LavinMQ ÉXITO: despacho #{} creado con tracking {}",
                    shipment.getId(), shipment.getTrackingNumber());
        } catch (Exception e) {
            log.error("LavinMQ ERROR en despacho del pedido {}: {}", event.orderId(), e.getMessage());
            throw new AmqpRejectAndDontRequeueException(e.getMessage(), e);
        }
    }
}
