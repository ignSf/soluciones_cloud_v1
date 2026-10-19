package com.example.inventory.messaging;

public record OrderCreatedEvent(
        Long orderId,
        String userEmail,
        Long productId,
        Integer quantity,
        Double totalAmount) {
}
