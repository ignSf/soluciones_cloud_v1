package com.example.notificaciones.service;

import com.example.notificaciones.model.NotificacionPedido;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Lógica de negocio: "enviar" la notificación al cliente.
 * Por ahora se simula con un log. Aquí se podría integrar Amazon SES para enviar un correo real.
 */
@Service
public class NotificacionService {

    private static final Logger log = LoggerFactory.getLogger(NotificacionService.class);

    private final boolean simularFallo;

    public NotificacionService(@Value("${app.notificaciones.simular-fallo}") boolean simularFallo) {
        this.simularFallo = simularFallo;
    }

    public void enviar(NotificacionPedido notificacion) {
        if (notificacion.pedidoId() == null) {
            throw new IllegalArgumentException("Mensaje sin pedidoId");
        }
        if (notificacion.email() == null || !notificacion.email().contains("@")) {
            throw new IllegalArgumentException("Email inválido para el pedido " + notificacion.pedidoId());
        }
        if (simularFallo) {
            throw new IllegalStateException("Fallo simulado del servicio de correo (SIMULAR_FALLO=true)");
        }

        log.info("NOTIFICACIÓN ENVIADA a {}: tu pedido #{} ({} x{}, total ${}) fue confirmado",
                notificacion.email(), notificacion.pedidoId(), notificacion.producto(),
                notificacion.cantidad(), notificacion.total());
    }
}