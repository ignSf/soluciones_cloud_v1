package com.example.inventory.messaging;

import com.example.inventory.config.RabbitConfig;
import com.example.inventory.model.InventoryItem;
import com.example.inventory.service.InventoryService;
import com.example.inventory.service.InventoryService.InsufficientStockException;
import com.example.inventory.service.InventoryService.ItemNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class OrderConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderConsumer.class);

    private final InventoryService inventoryService;

    public OrderConsumer(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @RabbitListener(queues = RabbitConfig.INVENTORY_QUEUE)
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("Evento recibido: pedido {} -> producto {} x{}",
                event.orderId(), event.productId(), event.quantity());
        try {
            InventoryItem item = inventoryService.reserveStock(event.productId(), event.quantity());
            log.info("Stock actualizado: producto {} ahora tiene {} unidades",
                    item.getProductId(), item.getStock());
        } catch (InsufficientStockException | ItemNotFoundException e) {
            log.warn("Pedido {} rechazado por inventario: {}", event.orderId(), e.getMessage());
            throw new AmqpRejectAndDontRequeueException(e.getMessage(), e);
        }
    }
}
