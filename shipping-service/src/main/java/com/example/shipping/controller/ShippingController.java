package com.example.shipping.controller;

import com.example.shipping.messaging.ShippingOrderEvent;
import com.example.shipping.model.Shipment;
import com.example.shipping.service.ShippingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/shipping")
@CrossOrigin(origins = "*")
public class ShippingController {

    private final ShippingService shippingService;

    public ShippingController(ShippingService shippingService) {
        this.shippingService = shippingService;
    }

    @GetMapping
    public List<Shipment> getAll() {
        return shippingService.getAllShipments();
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<Shipment> getByOrderId(@PathVariable Long orderId) {
        return shippingService.getShipmentByOrderId(orderId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/track/{trackingNumber}")
    public ResponseEntity<Shipment> getByTracking(@PathVariable String trackingNumber) {
        return shippingService.getShipmentByTracking(trackingNumber)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Shipment> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String newStatus = body.get("status");
        if (newStatus == null || newStatus.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        return shippingService.updateStatus(id, newStatus)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Endpoint de demostración: genera un despacho y dispara el evento en Kafka.
     */
    @PostMapping("/test-order")
    public ResponseEntity<Shipment> testOrder(
            @RequestParam(defaultValue = "101") Long orderId,
            @RequestParam(defaultValue = "Naruto Vol 2") String productName) {
        Shipment shipment = shippingService.processOrderShipping(
                new ShippingOrderEvent(
                        orderId,
                        "cliente@cloudstore.cl",
                        3L,
                        productName,
                        1,
                        15990.0
                )
        );
        return ResponseEntity.ok(shipment);
    }
}
