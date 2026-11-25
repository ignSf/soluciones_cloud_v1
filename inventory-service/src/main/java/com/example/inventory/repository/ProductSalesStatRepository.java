package com.example.inventory.repository;

import com.example.inventory.model.ProductSalesStat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductSalesStatRepository extends JpaRepository<ProductSalesStat, Long> {
    Optional<ProductSalesStat> findByProductId(Long productId);
}