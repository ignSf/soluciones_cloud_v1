package com.example.inventory.controller;

import com.example.inventory.model.InventoryItem;
import com.example.inventory.service.InventoryService;
import com.example.inventory.service.InventoryService.InsufficientStockException;
import com.example.inventory.service.InventoryService.ItemNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    public record CreateItem(@NotNull Long productId, String productName, @NotNull @Min(0) Integer stock) {}
    public record StockUpdate(@NotNull @Min(0) Integer stock) {}
    public record ReserveRequest(@NotNull @Min(1) Integer quantity) {}

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping
    public List<InventoryItem> getAll() {
        return inventoryService.findAll();
    }

    @GetMapping("/{productId}")
    public InventoryItem getByProduct(@PathVariable Long productId) {
        return inventoryService.findByProductId(productId);
    }

    @PostMapping
    public ResponseEntity<InventoryItem> create(@Valid @RequestBody CreateItem body) {
        InventoryItem created = inventoryService.create(body.productId(), body.productName(), body.stock());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{productId}")
    public InventoryItem setStock(@PathVariable Long productId, @Valid @RequestBody StockUpdate body) {
        return inventoryService.setStock(productId, body.stock());
    }

    @PostMapping("/{productId}/reserve")
    public InventoryItem reserve(@PathVariable Long productId, @Valid @RequestBody ReserveRequest body) {
        return inventoryService.reserveStock(productId, body.quantity());
    }

    @ExceptionHandler(ItemNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(ItemNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler({InsufficientStockException.class, IllegalStateException.class})
    public ResponseEntity<Map<String, String>> handleConflict(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
    }
}
