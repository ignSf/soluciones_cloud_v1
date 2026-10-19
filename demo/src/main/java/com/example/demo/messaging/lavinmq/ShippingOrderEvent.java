package com.example.demo.messaging.lavinmq;

public record ShippingOrderEvent(
        Long orderId,
        String userEmail,
        Long productId,
        String productName,
        Integer quantity,
        Double totalAmount
) {}
