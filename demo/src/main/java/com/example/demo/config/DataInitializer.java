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
                        .name("Yani Neko Vol 1")
                        .description("Primer tomo del manga Yani Neko (ChainSmoker Cat)")
                        .price(9.00)
                        .category("Seinen")
                        .build(),
                    Product.builder()
                        .name("Mirai Nikki Vol 6")
                        .description("Tomo 6 de Mirai Nikki")
                        .price(9.00)
                        .category("Romance, Terror")
                        .build(),
                    Product.builder()
                        .name("Naruto Vol 2")
                        .description("Tomo 2 de Naruto")
                        .price(7.00)
                        .category("Shonen")
                        .build()
                ));
            }
        };
    }
}
