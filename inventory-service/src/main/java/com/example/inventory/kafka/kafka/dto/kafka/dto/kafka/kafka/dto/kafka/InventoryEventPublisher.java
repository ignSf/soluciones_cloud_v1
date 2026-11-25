package com.example.inventory.kafka;

import com.example.inventory.config.KafkaTopicConfig;
import com.example.inventory.kafka.dto.StockChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class InventoryEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(InventoryEventPublisher.class);

    private final KafkaTemplate<String, StockChangedEvent> kafkaTemplate;

    public InventoryEventPublisher(KafkaTemplate<String, StockChangedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishStockChanged(StockChangedEvent event) {
        // La clave es el productId: los eventos de un mismo producto van a la misma partición
        String key = String.valueOf(event.productId());
        try {
            kafkaTemplate.send(KafkaTopicConfig.INVENTORY_TOPIC, key, event)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("No se pudo publicar StockChanged del producto {}: {}",
                                    event.productId(), ex.getMessage());
                        } else {
                            log.info("StockChanged publicado: producto {} -> {} unidades (partición {}, offset {})",
                                    event.productId(), event.newStock(),
                                    result.getRecordMetadata().partition(),
                                    result.getRecordMetadata().offset());
                        }
                    });
        } catch (Exception e) {
            // Si Kafka falla, el descuento de stock por RabbitMQ no debe verse afectado
            log.error("Error al enviar a Kafka el evento del producto {}: {}", event.productId(), e.getMessage());
        }
    }
}