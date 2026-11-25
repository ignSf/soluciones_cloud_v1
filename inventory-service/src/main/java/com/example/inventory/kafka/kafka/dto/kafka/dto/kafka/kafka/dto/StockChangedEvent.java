package com.example.inventory.kafka.dto;

import java.time.Instant;

public record StockChangedEvent(
        Long productId,
        String productName,
        Integer previousStock,
        Integer newStock,
        Long orderId,
        Instant changedAt) {
}