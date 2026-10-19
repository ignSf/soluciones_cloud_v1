package com.example.inventory.service;

import com.example.inventory.model.InventoryItem;
import com.example.inventory.repository.InventoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class InventoryService {

    private final InventoryRepository repository;

    public InventoryService(InventoryRepository repository) {
        this.repository = repository;
    }

    public List<InventoryItem> findAll() {
        return repository.findAll();
    }

    public InventoryItem findByProductId(Long productId) {
        return repository.findByProductId(productId)
                .orElseThrow(() -> new ItemNotFoundException(productId));
    }

    @Transactional
    public InventoryItem create(Long productId, String productName, int stock) {
        if (repository.findByProductId(productId).isPresent()) {
            throw new IllegalStateException("Ya existe inventario para el producto " + productId);
        }
        return repository.save(new InventoryItem(productId, productName, stock));
    }

    @Transactional
    public InventoryItem setStock(Long productId, int stock) {
        InventoryItem item = findByProductId(productId);
        item.setStock(stock);
        return repository.save(item);
    }

    @Transactional
    public InventoryItem reserveStock(Long productId, int quantity) {
        InventoryItem item = findByProductId(productId);
        if (item.getStock() < quantity) {
            throw new InsufficientStockException(productId, item.getStock(), quantity);
        }
        item.setStock(item.getStock() - quantity);
        return repository.save(item);
    }

    public static class ItemNotFoundException extends RuntimeException {
        public ItemNotFoundException(Long productId) {
            super("Producto sin inventario: " + productId);
        }
    }

    public static class InsufficientStockException extends RuntimeException {
        public InsufficientStockException(Long productId, int available, int requested) {
            super("Stock insuficiente para producto " + productId
                    + " (disponible: " + available + ", pedido: " + requested + ")");
        }
    }
}
