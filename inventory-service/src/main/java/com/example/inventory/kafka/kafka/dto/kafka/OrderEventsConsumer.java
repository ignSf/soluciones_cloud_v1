package com.example.inventory.kafka;

import com.example.inventory.config.KafkaTopicConfig;
import com.example.inventory.kafka.dto.OrderEventDTO;
import com.example.inventory.service.SalesStatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
public class OrderEventsConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderEventsConsumer.class);

    private final SalesStatService salesStatService;

    public OrderEventsConsumer(SalesStatService salesStatService) {
        this.salesStatService = salesStatService;
    }

    @KafkaListener(topics = KafkaTopicConfig.ORDERS_TOPIC, groupId = "inventory-group")
    public void onOrderEvent(OrderEventDTO event,
                             @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                             @Header(KafkaHeaders.OFFSET) long offset) {
        log.info("Evento de pedido recibido por Kafka: pedido {} -> producto {} x{} (partición {}, offset {})",
                event.orderId(), event.productId(), event.quantity(), partition, offset);
        salesStatService.recordSale(event);
    }
}