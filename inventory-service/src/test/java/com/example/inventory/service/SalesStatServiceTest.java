package com.example.inventory.service;

import com.example.inventory.kafka.dto.OrderEventDTO;
import com.example.inventory.model.ProductSalesStat;
import com.example.inventory.repository.ProductSalesStatRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SalesStatServiceTest {

    @Mock
    private ProductSalesStatRepository repository;

    @InjectMocks
    private SalesStatService service;

    @Test
    void recordSale_creaLaEstadisticaYSumaLasUnidades() {
        when(repository.findByProductId(3L)).thenReturn(Optional.empty());
        when(repository.save(any(ProductSalesStat.class))).thenAnswer(inv -> inv.getArgument(0));

        ProductSalesStat result = service.recordSale(
                new OrderEventDTO(1L, "a@a.cl", 3L, "Naruto Vol 2", 2, 14.0));

        assertEquals(2L, result.getUnitsSold().longValue());
        assertEquals(1L, result.getOrdersCount().longValue());
    }

    @Test
    void recordSale_acumulaSobreUnaEstadisticaExistente() {
        ProductSalesStat existing = new ProductSalesStat(3L, "Naruto Vol 2");
        existing.setUnitsSold(5L);
        existing.setOrdersCount(2L);
        when(repository.findByProductId(3L)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);

        ProductSalesStat result = service.recordSale(
                new OrderEventDTO(2L, "a@a.cl", 3L, "Naruto Vol 2", 3, 21.0));

        assertEquals(8L, result.getUnitsSold().longValue());
        assertEquals(3L, result.getOrdersCount().longValue());
    }

    @Test
    void recordSale_rechazaUnaCantidadNegativa() {
        OrderEventDTO invalido = new OrderEventDTO(3L, "a@a.cl", 3L, "Naruto Vol 2", -5, 1.0);

        assertThrows(IllegalArgumentException.class, () -> service.recordSale(invalido));
        verifyNoInteractions(repository);
    }
}
