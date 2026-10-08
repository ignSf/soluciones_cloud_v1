package com.example.demo.messaging.sqs;

import com.example.demo.model.Order;

/**
 * Mensaje que se publica en SQS cuando se crea un pedido.
 * Es un contrato entre "demo" (productor) y "notificaciones-service" (consumidor).
 */
public record NotificacionPedidoEvent(
        Long pedidoId,
        String email,
        String producto,
        Integer cantidad,
        Double total,
        String tipo) {

    public static final String TIPO_PEDIDO_CONFIRMADO = "PEDIDO_CONFIRMADO";

    public static NotificacionPedidoEvent desde(Order order) {
        return new NotificacionPedidoEvent(
                order.getId(),
                order.getUserEmail(),
                order.getProductName(),
                order.getQuantity(),
                order.getTotalAmount(),
                TIPO_PEDIDO_CONFIRMADO);
    }

    /** Serializa a JSON sin depender de la versión de Jackson del proyecto. */
    public String toJson() {
        return "{"
                + "\"pedidoId\":" + pedidoId + ","
                + "\"email\":" + texto(email) + ","
                + "\"producto\":" + texto(producto) + ","
                + "\"cantidad\":" + cantidad + ","
                + "\"total\":" + total + ","
                + "\"tipo\":" + texto(tipo)
                + "}";
    }

    private static String texto(String valor) {
        if (valor == null) {
            return "null";
        }
        String escapado = valor.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
        return "\"" + escapado + "\"";
    }
}