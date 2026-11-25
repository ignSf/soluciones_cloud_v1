package com.example.shipping.kafka.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.Instant;

/**
 * Evento consumido desde inventory.events (emitido por inventory-service al descontar stock).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record StockChangedEvent(
        Long productId,
        String productName,
        Integer previousStock,
        Integer newStock,
        Long orderId,
        Instant changedAt
) {
}
