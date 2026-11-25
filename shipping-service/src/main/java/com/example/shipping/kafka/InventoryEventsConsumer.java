package com.example.shipping.kafka;

import com.example.shipping.kafka.dto.StockChangedEvent;
import com.example.shipping.messaging.ShippingOrderEvent;
import com.example.shipping.model.Shipment;
import com.example.shipping.service.ShippingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Consumidor de eventos de cambio de stock publicados por inventory-service en Kafka (topic inventory.events).
 * Al confirmar la reserva de stock para un orderId, genera automáticamente el despacho logístico.
 */
@Component
public class InventoryEventsConsumer {

    private static final Logger log = LoggerFactory.getLogger(InventoryEventsConsumer.class);

    private final ShippingService shippingService;

    public InventoryEventsConsumer(ShippingService shippingService) {
        this.shippingService = shippingService;
    }

    @KafkaListener(topics = "inventory.events", groupId = "shipping-group")
    public void onStockChanged(StockChangedEvent event) {
        log.info("KAFKA CONSUMIDO (inventory.events): orderId={} productId={} producto={} stock={}->{}",
                event.orderId(),
                event.productId(),
                event.productName(),
                event.previousStock(),
                event.newStock());

        if (event.orderId() != null) {
            int qty = (event.previousStock() != null && event.newStock() != null)
                    ? Math.max(1, event.previousStock() - event.newStock())
                    : 1;

            ShippingOrderEvent shippingOrder = new ShippingOrderEvent(
                    event.orderId(),
                    "cliente@cloudstore.cl",
                    event.productId(),
                    event.productName(),
                    qty,
                    0.0
            );

            Shipment shipment = shippingService.processOrderShipping(shippingOrder);
            log.info("KAFKA EVENTO COMPLETADO -> DESPACHO CREADO: orderId={} tracking={}",
                    shipment.getOrderId(), shipment.getTrackingNumber());
        }
    }
}
