package com.example.inventory.kafka.dto;

public record OrderEventDTO(
        Long orderId,
        String userEmail,
        Long productId,
        String productName,
        Integer quantity,
        Double totalAmount) {
}