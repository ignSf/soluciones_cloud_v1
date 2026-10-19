package com.example.inventory.config;

import com.example.inventory.model.InventoryItem;
import com.example.inventory.repository.InventoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initInventory(InventoryRepository repository) {
        return args -> {
            if (repository.count() == 0) {
                repository.saveAll(List.of(
                        new InventoryItem(1L, "Yani Neko Vol 1", 30),
                        new InventoryItem(2L, "Mirai Nikki Vol 6", 20),
                        new InventoryItem(3L, "Naruto Vol 2", 40)
                ));
            }
        };
    }
}
