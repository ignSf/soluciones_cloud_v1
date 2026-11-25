package com.example.inventory.service;

import com.example.inventory.kafka.dto.OrderEventDTO;
import com.example.inventory.model.ProductSalesStat;
import com.example.inventory.repository.ProductSalesStatRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SalesStatService {

    private final ProductSalesStatRepository repository;

    public SalesStatService(ProductSalesStatRepository repository) {
        this.repository = repository;
    }

    @Transactional
    
    public ProductSalesStat recordSale(OrderEventDTO event) {
        if (event.productId() == null || event.quantity() == null || event.quantity() <= 0) {
            throw new IllegalArgumentException("Pedido inválido: " + event);
        }
        ProductSalesStat stat = repository.findByProductId(event.productId())
                .orElseGet(() -> new ProductSalesStat(event.productId(), event.productName()));
        stat.setUnitsSold(stat.getUnitsSold() + event.quantity());
        stat.setOrdersCount(stat.getOrdersCount() + 1);
        return repository.save(stat);
    }

    public List<ProductSalesStat> findAll() {
        return repository.findAll();
    }
}