package com.example.shipping.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Declara los topics de Kafka propios del microservicio de despachos.
 */
@Configuration
public class KafkaTopicConfig {

    public static final String TOPIC_SHIPPING_EVENTS = "shipping.events";
    public static final String TOPIC_SHIPPING_INVENTORY_DLT = "shipping.inventory.DLT";

    @Bean
    public NewTopic shippingEventsTopic() {
        return TopicBuilder.name(TOPIC_SHIPPING_EVENTS)
                .partitions(3)
                .replicas(2)
                .build();
    }

    @Bean
    public NewTopic shippingInventoryDltTopic() {
        return TopicBuilder.name(TOPIC_SHIPPING_INVENTORY_DLT)
                .partitions(3)
                .replicas(2)
                .build();
    }
}
