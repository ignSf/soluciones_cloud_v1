package com.example.shipping.messaging;

public record ShippingOrderEvent(
        Long orderId,
        String userEmail,
        Long productId,
        String productName,
        Integer quantity,
        Double totalAmount
) {}
