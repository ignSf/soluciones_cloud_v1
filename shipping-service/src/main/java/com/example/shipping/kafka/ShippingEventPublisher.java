package com.example.shipping.kafka;

import com.example.shipping.config.KafkaTopicConfig;
import com.example.shipping.kafka.dto.ShippingStatusEvent;
import com.example.shipping.model.Shipment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Publicador de eventos de despacho hacia Apache Kafka (topic shipping.events).
 */
@Component
public class ShippingEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(ShippingEventPublisher.class);

    private final KafkaTemplate<String, ShippingStatusEvent> kafkaTemplate;

    public ShippingEventPublisher(KafkaTemplate<String, ShippingStatusEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishShippingStatus(Shipment shipment) {
        ShippingStatusEvent event = new ShippingStatusEvent(
                shipment.getId(),
                shipment.getOrderId(),
                shipment.getTrackingNumber(),
                shipment.getUserEmail(),
                shipment.getProductId(),
                shipment.getProductName(),
                shipment.getStatus(),
                shipment.getCarrier(),
                Instant.now()
        );

        String key = shipment.getOrderId() != null ? shipment.getOrderId().toString() : shipment.getId().toString();
        try {
            kafkaTemplate.send(KafkaTopicConfig.TOPIC_SHIPPING_EVENTS, key, event)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.warn("KAFKA ERROR al publicar ShippingStatusEvent | orderId={} motivo={}",
                                    shipment.getOrderId(), ex.getMessage());
                        } else if (result != null && result.getRecordMetadata() != null) {
                            log.info("KAFKA OK: ShippingStatusEvent publicado | topic={} partition={} offset={} orderId={} status={}",
                                    result.getRecordMetadata().topic(),
                                    result.getRecordMetadata().partition(),
                                    result.getRecordMetadata().offset(),
                                    shipment.getOrderId(),
                                    shipment.getStatus());
                        }
                    });
        } catch (Exception ex) {
            // No bloquear la operación HTTP/DB si Kafka no está disponible
            log.warn("KAFKA NO DISPONIBLE al publicar ShippingStatusEvent | orderId={} motivo={}",
                    shipment.getOrderId(), ex.getMessage());
        }
    }
}
