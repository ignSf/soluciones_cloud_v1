package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;

/**
 * Configuración de Amazon SQS.
 * Las credenciales NO van aquí: el SDK las toma de la cadena por defecto
 * (variables de entorno, ~/.aws/credentials o el rol de la instancia EC2).
 */
@Configuration
public class SqsConfig {

    @Bean(destroyMethod = "close")
    public SqsClient sqsClient(@Value("${app.sqs.region}") String region) {
        return SqsClient.builder()
                .region(Region.of(region))
                .build();
    }
}