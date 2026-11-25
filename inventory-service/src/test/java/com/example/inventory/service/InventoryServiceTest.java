package com.example.inventory.service;

import com.example.inventory.model.InventoryItem;
import com.example.inventory.repository.InventoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryRepository repository;

    @InjectMocks
    private InventoryService service;

    @Test
    void reserveStock_descuentaElStock() {
        InventoryItem item = new InventoryItem(3L, "Naruto Vol 2", 40);
        when(repository.findByProductId(3L)).thenReturn(Optional.of(item));
        when(repository.save(item)).thenReturn(item);

        InventoryItem result = service.reserveStock(3L, 2);

        assertEquals(38, result.getStock().intValue());
    }

    @Test
    void reserveStock_sinStockSuficienteLanzaExcepcionYNoGuarda() {
        InventoryItem item = new InventoryItem(3L, "Naruto Vol 2", 1);
        when(repository.findByProductId(3L)).thenReturn(Optional.of(item));

        assertThrows(InventoryService.InsufficientStockException.class,
                () -> service.reserveStock(3L, 5));
        verify(repository, never()).save(any());
    }

    @Test
    void reserveStock_productoInexistenteLanzaExcepcion() {
        when(repository.findByProductId(99L)).thenReturn(Optional.empty());

        assertThrows(InventoryService.ItemNotFoundException.class,
                () -> service.reserveStock(99L, 1));
    }
}
