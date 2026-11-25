package com.example.inventory.controller;

import com.example.inventory.model.ProductSalesStat;
import com.example.inventory.service.SalesStatService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/inventory/sales")
public class SalesStatController {

    private final SalesStatService salesStatService;

    public SalesStatController(SalesStatService salesStatService) {
        this.salesStatService = salesStatService;
    }

    @GetMapping
    public List<ProductSalesStat> getAll() {
        return salesStatService.findAll();
    }
}