package com.example.notificaciones.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;

/**
 * Cliente SQS. Las credenciales se toman de la cadena por defecto del SDK
 * (variables de entorno en local, rol LabInstanceProfile en EC2). Nunca en el código.
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