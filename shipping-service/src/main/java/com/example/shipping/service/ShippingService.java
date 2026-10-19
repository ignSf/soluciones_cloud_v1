package com.example.shipping.service;

import com.example.shipping.messaging.ShippingOrderEvent;
import com.example.shipping.model.Shipment;
import com.example.shipping.repository.ShipmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ShippingService {

    private static final Logger log = LoggerFactory.getLogger(ShippingService.class);

    private final ShipmentRepository shipmentRepository;

    public ShippingService(ShipmentRepository shipmentRepository) {
        this.shipmentRepository = shipmentRepository;
    }

    public Shipment processOrderShipping(ShippingOrderEvent event) {
        if (event.orderId() == null) {
            throw new IllegalArgumentException("Evento de envío sin orderId");
        }

        // Idempotencia: evitar despachos duplicados si el evento de LavinMQ se reintenta
        Optional<Shipment> existente = shipmentRepository.findByOrderId(event.orderId());
        if (existente.isPresent()) {
            log.info("Despacho ya existente para pedidoId={}", event.orderId());
            return existente.get();
        }

        String tracking = "TRK-CL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Shipment shipment = new Shipment();
        shipment.setOrderId(event.orderId());
        shipment.setUserEmail(event.userEmail() != null ? event.userEmail() : "cliente@cloudstore.local");
        shipment.setProductId(event.productId());
        shipment.setProductName(event.productName() != null ? event.productName() : "Producto #" + event.productId());
        shipment.setQuantity(event.quantity() != null ? event.quantity() : 1);
        shipment.setTrackingNumber(tracking);
        shipment.setStatus("PREPARING");
        shipment.setCarrier("CloudStore Express Logística");
        shipment.setDestinationAddress("Centro de Distribución Santiago Centro (Chile)");
        shipment.setEstimatedDelivery(LocalDateTime.now().plusDays(2));
        shipment.setCreatedAt(LocalDateTime.now());

        Shipment saved = shipmentRepository.save(shipment);
        log.info("LavinMQ DESPACHO GENERADO: pedidoId={} tracking={} transportista={}",
                saved.getOrderId(), saved.getTrackingNumber(), saved.getCarrier());
        return saved;
    }

    public List<Shipment> getAllShipments() {
        return shipmentRepository.findAllByOrderByCreatedAtDesc();
    }

    public Optional<Shipment> getShipmentByOrderId(Long orderId) {
        return shipmentRepository.findByOrderId(orderId);
    }

    public Optional<Shipment> getShipmentByTracking(String trackingNumber) {
        return shipmentRepository.findByTrackingNumber(trackingNumber);
    }

    public Optional<Shipment> updateStatus(Long id, String newStatus) {
        return shipmentRepository.findById(id).map(s -> {
            s.setStatus(newStatus);
            return shipmentRepository.save(s);
        });
    }
}
