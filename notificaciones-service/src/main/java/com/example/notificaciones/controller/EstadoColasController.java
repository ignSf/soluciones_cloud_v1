package com.example.notificaciones.controller;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.GetQueueAttributesRequest;
import software.amazon.awssdk.services.sqs.model.QueueAttributeName;

/**
 * Endpoint de monitoreo: muestra cuántos mensajes hay en la cola principal y en la DLQ.
 * Útil para la demostración (los contadores de SQS son aproximados).
 */
@RestController
@RequestMapping("/api/notificaciones")
public class EstadoColasController {

    private final SqsClient sqs;
    private final String queueUrl;
    private final String dlqUrl;

    public EstadoColasController(SqsClient sqs,
            @Value("${app.sqs.notificaciones.queue-url}") String queueUrl,
            @Value("${app.sqs.notificaciones.dlq-url}") String dlqUrl) {
        this.sqs = sqs;
        this.queueUrl = queueUrl;
        this.dlqUrl = dlqUrl;
    }

    @GetMapping("/colas/estado")
    public Map<String, Object> estado() {
        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("cola", contar(queueUrl));
        respuesta.put("dlq", contar(dlqUrl));
        return respuesta;
    }

    private Map<String, String> contar(String url) {
        if (url == null || url.isBlank()) {
            return Map.of("estado", "URL no configurada");
        }
        var atributos = sqs.getQueueAttributes(GetQueueAttributesRequest.builder()
                .queueUrl(url)
                .attributeNames(QueueAttributeName.APPROXIMATE_NUMBER_OF_MESSAGES,
                        QueueAttributeName.APPROXIMATE_NUMBER_OF_MESSAGES_NOT_VISIBLE)
                .build()).attributes();
        return Map.of(
                "visibles", atributos.getOrDefault(QueueAttributeName.APPROXIMATE_NUMBER_OF_MESSAGES, "0"),
                "noVisibles", atributos.getOrDefault(QueueAttributeName.APPROXIMATE_NUMBER_OF_MESSAGES_NOT_VISIBLE, "0"));
    }
}