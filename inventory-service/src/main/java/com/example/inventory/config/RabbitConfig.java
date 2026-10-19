package com.example.inventory.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String ORDERS_EXCHANGE = "orders.exchange";
    public static final String ORDER_CREATED_KEY = "orders.created";

    public static final String INVENTORY_QUEUE = "inventory.order-created.queue";

    public static final String DLX = "inventory.dlx";
    public static final String DLQ = "inventory.order-created.dlq";
    public static final String DLQ_KEY = "inventory.order-created.dead";

    @Bean
    public DirectExchange ordersExchange() {
        return new DirectExchange(ORDERS_EXCHANGE);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DLX);
    }

    @Bean
    public Queue inventoryQueue() {
        return QueueBuilder.durable(INVENTORY_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX)
                .withArgument("x-dead-letter-routing-key", DLQ_KEY)
                .build();
    }

    @Bean
    public Queue inventoryDeadLetterQueue() {
        return QueueBuilder.durable(DLQ).build();
    }

    @Bean
    public Binding inventoryBinding(Queue inventoryQueue, DirectExchange ordersExchange) {
        return BindingBuilder.bind(inventoryQueue).to(ordersExchange).with(ORDER_CREATED_KEY);
    }

    @Bean
    public Binding deadLetterBinding(Queue inventoryDeadLetterQueue, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(inventoryDeadLetterQueue).to(deadLetterExchange).with(DLQ_KEY);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
