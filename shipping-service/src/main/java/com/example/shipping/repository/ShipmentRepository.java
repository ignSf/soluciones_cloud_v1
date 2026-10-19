package com.example.shipping.repository;

import com.example.shipping.model.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, Long> {
    Optional<Shipment> findByOrderId(Long orderId);
    Optional<Shipment> findByTrackingNumber(String trackingNumber);
    List<Shipment> findByUserEmailOrderByCreatedAtDesc(String userEmail);
    List<Shipment> findAllByOrderByCreatedAtDesc();
}
