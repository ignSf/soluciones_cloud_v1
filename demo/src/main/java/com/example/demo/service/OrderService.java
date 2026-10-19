package com.example.demo.service;

import com.example.demo.messaging.OrderEventPublisher;
import com.example.demo.messaging.sqs.SqsNotificacionPublisher;
import com.example.demo.messaging.lavinmq.LavinMqShippingPublisher;
import com.example.demo.model.Order;
import com.example.demo.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    
    // Bus 1: RabbitMQ -> inventory-service (Trabajo Compañero 1: Descuento de stock)
    private final OrderEventPublisher orderEventPublisher;

    // Bus 2: Amazon SQS -> notificaciones-service (Trabajo Compañero 2: Notificaciones y correos)
    private final SqsNotificacionPublisher sqsNotificacionPublisher;

    // Bus 3: LavinMQ -> shipping-service (Su trabajo: Logística, despacho y tracking)
    private final LavinMqShippingPublisher lavinMqShippingPublisher;

    public Order createOrder(Order order) {
        Order saved = orderRepository.save(order);

        // 1. Notifica a RabbitMQ para descontar el stock en inventory-service
        orderEventPublisher.publishOrderCreated(saved);

        // 2. Notifica a Amazon SQS para procesar el correo en notificaciones-service
        sqsNotificacionPublisher.publicarPedidoCreado(saved);

        // 3. Notifica a LavinMQ para generar la orden de despacho en shipping-service
        lavinMqShippingPublisher.publicarDespachoPedido(saved);

        return saved;
    }

    public List<Order> getOrdersByUser(String userEmail) {
        return orderRepository.findByUserEmailOrderByCreatedAtDesc(userEmail);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc();
    }

    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findById(id);
    }
}
