package com.example.demo.service;

import com.example.demo.model.Order;
import com.example.demo.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.example.demo.messaging.sqs.SqsNotificacionPublisher;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    private final SqsNotificacionPublisher sqsNotificacionPublisher;

        public Order createOrder(Order order) {
        Order saved = orderRepository.save(order);
        sqsNotificacionPublisher.publicarPedidoCreado(saved);
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
