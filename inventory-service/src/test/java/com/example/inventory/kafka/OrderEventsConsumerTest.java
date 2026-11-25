package com.example.inventory.kafka;

import com.example.inventory.kafka.dto.OrderEventDTO;
import com.example.inventory.service.SalesStatService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderEventsConsumerTest {

    @Mock
    private SalesStatService salesStatService;

    @InjectMocks
    private OrderEventsConsumer consumer;

    @Test
    void onOrderEvent_delegaElEventoAlServicioDeNegocio() {
        OrderEventDTO event = new OrderEventDTO(1L, "a@a.cl", 3L, "Naruto Vol 2", 2, 14.0);

        consumer.onOrderEvent(event, 0, 10L);

        verify(salesStatService).recordSale(event);
    }
}
