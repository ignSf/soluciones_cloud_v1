package com.example.demo.config;

import com.example.demo.model.Product;
import com.example.demo.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initDatabase(ProductRepository productRepository) {
        return args -> {
            if (productRepository.count() == 0) {
                productRepository.saveAll(List.of(
                    Product.builder()
                        .name("Laptop Dell XPS 15")
                        .description("Intel Core i7, 16GB RAM, 512GB SSD")
                        .price(1499.99)
                        .category("Hardware")
                        .build(),
                    Product.builder()
                        .name("Monitor LG UltraWide 34\"")
                        .description("Resolución WQHD 144Hz IPS")
                        .price(599.50)
                        .category("Periféricos")
                        .build(),
                    Product.builder()
                        .name("Suscripción Azure Cloud")
                        .description("Créditos de cómputo y servicios cognitivos")
                        .price(250.00)
                        .category("Servicios Cloud")
                        .build()
                ));
            }
        };
    }
}
