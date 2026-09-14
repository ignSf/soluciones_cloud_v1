package com.example.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userEmail;

    private Long productId;

    private String productName;

    private Integer quantity;

    private Double unitPrice;

    private Double totalAmount;

    @Builder.Default
    private String status = "CONFIRMED";

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.status == null || this.status.isBlank()) {
            this.status = "CONFIRMED";
        }
        if (this.quantity == null || this.quantity <= 0) {
            this.quantity = 1;
        }
        if (this.totalAmount == null && this.unitPrice != null) {
            this.totalAmount = this.unitPrice * this.quantity;
        }
    }
}
