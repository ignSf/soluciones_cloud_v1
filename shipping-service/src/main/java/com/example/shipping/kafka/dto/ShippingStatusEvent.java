package com.example.shipping.kafka.dto;

import java.time.Instant;

/**
 * Evento publicado en Kafka (topic shipping.events) cada vez que se crea o cambia el estado de un despacho.
 */
public record ShippingStatusEvent(
        Long shipmentId,
        Long orderId,
        String trackingNumber,
        String userEmail,
        Long productId,
        String productName,
        String status,
        String carrier,
        Instant occurredAt
) {
}
