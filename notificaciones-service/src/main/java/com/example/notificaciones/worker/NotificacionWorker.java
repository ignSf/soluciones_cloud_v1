package com.example.notificaciones.worker;

import com.example.notificaciones.model.NotificacionPedido;
import com.example.notificaciones.service.NotificacionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.MessageSystemAttributeName;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

/**
 * Consumidor SQS (long polling).
 *
 * Ciclo de cada mensaje:
 *  1. ReceiveMessage  -> el mensaje queda NO visible durante el visibility timeout.
 *  2. Procesar        -> NotificacionService.enviar(...)
 *  3a. Éxito          -> DeleteMessage con el receiptHandle (equivale al ACK de RabbitMQ).
 *  3b. Error          -> NO se borra. Vuelve a ser visible y se reintenta.
 *                        Al superar maxReceiveCount, SQS lo mueve a la DLQ (redrive policy).
 */
@Component
public class NotificacionWorker {

    private static final Logger log = LoggerFactory.getLogger(NotificacionWorker.class);

    private final SqsClient sqs;
    private final ObjectMapper json;
    private final NotificacionService notificacionService;
    private final String queueUrl;
    private final boolean enabled;
    private final int maxMensajes;
    private final int esperaSegundos;

    public NotificacionWorker(SqsClient sqs, ObjectMapper json, NotificacionService notificacionService,
            @Value("${app.sqs.notificaciones.queue-url}") String queueUrl,
            @Value("${app.sqs.notificaciones.consumer-enabled}") boolean enabled,
            @Value("${app.sqs.notificaciones.max-mensajes}") int maxMensajes,
            @Value("${app.sqs.notificaciones.wait-seconds}") int esperaSegundos) {
        this.sqs = sqs;
        this.json = json;
        this.notificacionService = notificacionService;
        this.queueUrl = queueUrl;
        this.enabled = enabled;
        this.maxMensajes = maxMensajes;
        this.esperaSegundos = esperaSegundos;
    }

    @Scheduled(fixedDelayString = "${app.sqs.notificaciones.poll-delay-ms}")
    public void sondear() {
        if (!enabled || queueUrl == null || queueUrl.isBlank()) {
            return;
        }
        try {
            var mensajes = sqs.receiveMessage(ReceiveMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .maxNumberOfMessages(maxMensajes)
                    .waitTimeSeconds(esperaSegundos)
                    .messageSystemAttributeNames(MessageSystemAttributeName.APPROXIMATE_RECEIVE_COUNT)
                    .build()).messages();
            mensajes.forEach(this::procesar);
        } catch (Exception problemaDeConexion) {
            log.error("No se pudo sondear SQS: {}", problemaDeConexion.toString());
        }
    }

    private void procesar(Message m) {
        String intento = m.attributes().getOrDefault(MessageSystemAttributeName.APPROXIMATE_RECEIVE_COUNT, "?");
        try {
            NotificacionPedido notificacion = json.readValue(m.body(), NotificacionPedido.class);
            notificacionService.enviar(notificacion);

            sqs.deleteMessage(DeleteMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .receiptHandle(m.receiptHandle())
                    .build());
            log.info("BORRADO messageId={} pedidoId={} recepción={}", m.messageId(), notificacion.pedidoId(), intento);
        } catch (Exception fallo) {
            log.warn("SIN BORRAR messageId={} recepción={} motivo={} -> se reintentará; si supera maxReceiveCount irá a la DLQ",
                    m.messageId(), intento, fallo.getMessage());
        }
    }
}