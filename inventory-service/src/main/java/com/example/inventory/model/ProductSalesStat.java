package com.example.inventory.model;

import jakarta.persistence.*;

@Entity
@Table(name = "product_sales_stats")
public class ProductSalesStat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long productId;

    private String productName;

    @Column(nullable = false)
    private Long unitsSold = 0L;

    @Column(nullable = false)
    private Long ordersCount = 0L;

    // Si dos hilos actualizan el mismo producto a la vez, JPA detecta el conflicto
    @Version
    private Long version;

    public ProductSalesStat() {}

    public ProductSalesStat(Long productId, String productName) {
        this.productId = productId;
        this.productName = productName;
    }

    public Long getId() { return id; }
    public Long getProductId() { return productId; }
    public String getProductName() { return productName; }
    public Long getUnitsSold() { return unitsSold; }
    public void setUnitsSold(Long unitsSold) { this.unitsSold = unitsSold; }
    public Long getOrdersCount() { return ordersCount; }
    public void setOrdersCount(Long ordersCount) { this.ordersCount = ordersCount; }
}