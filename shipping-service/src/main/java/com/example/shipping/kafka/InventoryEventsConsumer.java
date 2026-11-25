package com.example.shipping.kafka;

import com.example.shipping.kafka.dto.StockChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Consumidor de eventos de cambio de stock publicados por inventory-service en Kafka.
 */
@Component
public class InventoryEventsConsumer {

    private static final Logger log = LoggerFactory.getLogger(InventoryEventsConsumer.class);

    @KafkaListener(topics = "inventory.events", groupId = "shipping-group")
    public void onStockChanged(StockChangedEvent event) {
        log.info("KAFKA CONSUMIDO (inventory.events): productId={} producto={} stock={}->{} motivo={}",
                event.productId(),
                event.productName(),
                event.previousStock(),
                event.newStock(),
                event.reason());
    }
}
