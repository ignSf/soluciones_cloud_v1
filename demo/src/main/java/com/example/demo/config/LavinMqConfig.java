package com.example.demo.config;

import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración para el Bus 3: LavinMQ (AMQP 0-9-1)
 * Gestiona la conexión independiente hacia el broker de envíos y logística (shipping-service).
 */
@Configuration
public class LavinMqConfig {

    public static final String SHIPPING_EXCHANGE = "shipping.exchange";
    public static final String SHIPPING_CREATED_KEY = "shipping.created";

    @Bean
    public ConnectionFactory lavinMqConnectionFactory(
            @Value("${app.lavinmq.host:localhost}") String host,
            @Value("${app.lavinmq.port:5673}") int port,
            @Value("${app.lavinmq.username:guest}") String username,
            @Value("${app.lavinmq.password:guest}") String password) {
        CachingConnectionFactory factory = new CachingConnectionFactory();
        factory.setHost(host);
        factory.setPort(port);
        factory.setUsername(username);
        factory.setPassword(password);
        return factory;
    }

    @Bean(name = "lavinMqRabbitTemplate")
    public RabbitTemplate lavinMqRabbitTemplate(ConnectionFactory lavinMqConnectionFactory) {
        RabbitTemplate template = new RabbitTemplate(lavinMqConnectionFactory);
        template.setMessageConverter(new JacksonJsonMessageConverter());
        return template;
    }
}
