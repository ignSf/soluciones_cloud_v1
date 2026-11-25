package com.example.inventory.messaging;

import com.example.inventory.config.RabbitConfig;
import com.example.inventory.kafka.InventoryEventPublisher;
import com.example.inventory.kafka.dto.StockChangedEvent;
import com.example.inventory.model.InventoryItem;
import com.example.inventory.service.InventoryService;
import com.example.inventory.service.InventoryService.InsufficientStockException;
import com.example.inventory.service.InventoryService.ItemNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class OrderConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderConsumer.class);

    private final InventoryService inventoryService;
    private final InventoryEventPublisher inventoryEventPublisher;

    public OrderConsumer(InventoryService inventoryService,
                         InventoryEventPublisher inventoryEventPublisher) {
        this.inventoryService = inventoryService;
        this.inventoryEventPublisher = inventoryEventPublisher;
    }

    @RabbitListener(queues = RabbitConfig.INVENTORY_QUEUE)
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("Evento recibido: pedido {} -> producto {} x{}",
                event.orderId(), event.productId(), event.quantity());
        try {
            InventoryItem item = inventoryService.reserveStock(event.productId(), event.quantity());
            log.info("Stock actualizado: producto {} ahora tiene {} unidades",
                    item.getProductId(), item.getStock());

            // Se informa a Kafka del cambio de stock (stream de eventos para otros servicios)
            inventoryEventPublisher.publishStockChanged(new StockChangedEvent(
                    item.getProductId(),
                    item.getProductName(),
                    item.getStock() + event.quantity(),
                    item.getStock(),
                    event.orderId(),
                    Instant.now()));
        } catch (InsufficientStockException | ItemNotFoundException e) {
            log.warn("Pedido {} rechazado por inventario: {}", event.orderId(), e.getMessage());
            // No reencolar: el mensaje se va a la Dead Letter Queue
            throw new AmqpRejectAndDontRequeueException(e.getMessage(), e);
        }
    }
}