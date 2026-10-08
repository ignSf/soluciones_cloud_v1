package com.example.notificaciones.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Mensaje recibido desde SQS. Debe coincidir con NotificacionPedidoEvent del backend "demo".
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record NotificacionPedido(
        Long pedidoId,
        String email,
        String producto,
        Integer cantidad,
        Double total,
        String tipo) {
}