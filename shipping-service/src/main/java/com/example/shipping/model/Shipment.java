package com.example.shipping.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "shipments")
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long orderId;

    private String userEmail;
    private Long productId;
    private String productName;
    private Integer quantity;

    @Column(nullable = false, unique = true)
    private String trackingNumber;

    @Column(nullable = false)
    private String status; // PREPARING, IN_TRANSIT, DELIVERED

    private String carrier;
    private String destinationAddress;
    private LocalDateTime estimatedDelivery;
    private LocalDateTime createdAt;

    public Shipment() {
    }

    public Shipment(Long orderId, String userEmail, Long productId, String productName, Integer quantity,
                    String trackingNumber, String status, String carrier, String destinationAddress,
                    LocalDateTime estimatedDelivery, LocalDateTime createdAt) {
        this.orderId = orderId;
        this.userEmail = userEmail;
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.trackingNumber = trackingNumber;
        this.status = status;
        this.carrier = carrier;
        this.destinationAddress = destinationAddress;
        this.estimatedDelivery = estimatedDelivery;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCarrier() {
        return carrier;
    }

    public void setCarrier(String carrier) {
        this.carrier = carrier;
    }

    public String getDestinationAddress() {
        return destinationAddress;
    }

    public void setDestinationAddress(String destinationAddress) {
        this.destinationAddress = destinationAddress;
    }

    public LocalDateTime getEstimatedDelivery() {
        return estimatedDelivery;
    }

    public void setEstimatedDelivery(LocalDateTime estimatedDelivery) {
        this.estimatedDelivery = estimatedDelivery;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
