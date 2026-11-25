package com.example.inventory.kafka;

import com.example.inventory.config.KafkaTopicConfig;
import com.example.inventory.kafka.dto.StockChangedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryEventPublisherTest {

    @Mock
    private KafkaTemplate<String, StockChangedEvent> kafkaTemplate;

    @InjectMocks
    private InventoryEventPublisher publisher;

    private final StockChangedEvent event =
            new StockChangedEvent(3L, "Naruto Vol 2", 40, 38, 201L, Instant.now());

    @Test
    void publishStockChanged_usaElProductIdComoClaveYElTopicDeInventario() {
        // Un futuro que nunca se completa: el callback de logging no se ejecuta en la prueba
        when(kafkaTemplate.send(KafkaTopicConfig.INVENTORY_TOPIC, "3", event))
                .thenReturn(new CompletableFuture<>());

        publisher.publishStockChanged(event);

        verify(kafkaTemplate).send(KafkaTopicConfig.INVENTORY_TOPIC, "3", event);
    }

    @Test
    void publishStockChanged_siKafkaFallaNoPropagaLaExcepcion() {
        when(kafkaTemplate.send(anyString(), anyString(), any(StockChangedEvent.class)))
                .thenThrow(new RuntimeException("Kafka caído"));

        // El descuento de stock por RabbitMQ no debe verse afectado por una caída de Kafka
        assertDoesNotThrow(() -> publisher.publishStockChanged(event));
    }
}
