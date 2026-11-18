package com.example.notificaciones.service;

import com.example.notificaciones.model.NotificacionPedido;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class NotificacionService {

    private static final Logger log = LoggerFactory.getLogger(NotificacionService.class);

    private final JavaMailSender mailSender;
    private final boolean simularFallo;
    private final String remitente;

    public NotificacionService(JavaMailSender mailSender,
            @Value("${app.notificaciones.simular-fallo}") boolean simularFallo,
            @Value("${spring.mail.username}") String remitente) {
        this.mailSender = mailSender;
        this.simularFallo = simularFallo;
        this.remitente = remitente;
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

        SimpleMailMessage correo = new SimpleMailMessage();
        correo.setFrom(remitente);
        correo.setTo(notificacion.email());
        correo.setSubject("Pedido #" + notificacion.pedidoId() + " confirmado");
        correo.setText("Hola!\n\nTu pedido #" + notificacion.pedidoId() + " fue confirmado.\n\n"
                + "Producto: " + notificacion.producto() + "\n"
                + "Cantidad: " + notificacion.cantidad() + "\n"
                + "Total: $" + notificacion.total() + "\n\nGracias por tu compra.");

        // Si el SMTP falla, la excepción sube al worker: no se borra el mensaje → reintento → DLQ
        mailSender.send(correo);

        log.info("CORREO ENVIADO a {} para pedido #{}", notificacion.email(), notificacion.pedidoId());
    }
}