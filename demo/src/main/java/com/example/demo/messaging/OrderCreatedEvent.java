package com.example.demo.messaging;

/**
 * Evento que se publica cuando se crea un pedido.
 * Los nombres de los campos forman el JSON que lee inventory-service.
 */
public record OrderCreatedEvent(
        Long orderId,
        String userEmail,
        Long productId,
        Integer quantity,
        Double totalAmount) {
}
