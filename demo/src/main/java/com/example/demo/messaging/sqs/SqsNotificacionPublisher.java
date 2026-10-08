package com.example.demo.messaging.sqs;

import com.example.demo.model.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

/**
 * Productor SQS: publica un mensaje en la cola de notificaciones
 * cada vez que se crea un pedido.
 *
 * Regla clave: si SQS falla, el pedido NO se cae. Solo se registra el error,
 * porque la notificación es una tarea secundaria y asíncrona.
 */
@Component
public class SqsNotificacionPublisher {

    private static final Logger log = LoggerFactory.getLogger(SqsNotificacionPublisher.class);

    private final SqsClient sqs;
    private final boolean enabled;
    private final String queueUrl;

    public SqsNotificacionPublisher(SqsClient sqs,
            @Value("${app.sqs.notificaciones.enabled}") boolean enabled,
            @Value("${app.sqs.notificaciones.queue-url}") String queueUrl) {
        this.sqs = sqs;
        this.enabled = enabled;
        this.queueUrl = queueUrl;
    }

    public void publicarPedidoCreado(Order order) {
        if (!enabled || queueUrl == null || queueUrl.isBlank()) {
            log.info("SQS desactivado: no se publica notificación del pedido {}", order.getId());
            return;
        }
        NotificacionPedidoEvent evento = NotificacionPedidoEvent.desde(order);
        try {
            var respuesta = sqs.sendMessage(SendMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .messageBody(evento.toJson())
                    .build());
            log.info("SQS PUBLICADO pedidoId={} messageId={}", order.getId(), respuesta.messageId());
        } catch (Exception e) {
            log.error("SQS ERROR al publicar pedidoId={}: {}", order.getId(), e.toString());
        }
    }
}