package com.example.shipping.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de LavinMQ para el microservicio de envíos (shipping-service).
 * Declara el intercambio, colas duraderas y Dead Letter Queue (DLQ) para resiliencia.
 */
@Configuration
public class LavinMqConfig {

    public static final String SHIPPING_EXCHANGE = "shipping.exchange";
    public static final String SHIPPING_CREATED_KEY = "shipping.created";

    public static final String SHIPPING_QUEUE = "shipping.order-created.queue";

    public static final String DLX = "shipping.dlx";
    public static final String DLQ = "shipping.order-created.dlq";
    public static final String DLQ_KEY = "shipping.order-created.dead";

    @Bean
    public DirectExchange shippingExchange() {
        return new DirectExchange(SHIPPING_EXCHANGE);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DLX);
    }

    @Bean
    public Queue shippingQueue() {
        return QueueBuilder.durable(SHIPPING_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX)
                .withArgument("x-dead-letter-routing-key", DLQ_KEY)
                .build();
    }

    @Bean
    public Queue shippingDeadLetterQueue() {
        return QueueBuilder.durable(DLQ).build();
    }

    @Bean
    public Binding shippingBinding(Queue shippingQueue, DirectExchange shippingExchange) {
        return BindingBuilder.bind(shippingQueue).to(shippingExchange).with(SHIPPING_CREATED_KEY);
    }

    @Bean
    public Binding deadLetterBinding(Queue shippingDeadLetterQueue, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(shippingDeadLetterQueue).to(deadLetterExchange).with(DLQ_KEY);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
