package com.orderengine.controller;

import com.orderengine.dto.CreateOrderRequest;
import com.orderengine.dto.OrderResponse;
import com.orderengine.dto.SystemMetricsDTO;
import com.orderengine.service.MetricsService;
import com.orderengine.service.OrderProcessingService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    private final OrderProcessingService orderProcessingService;
    private final MetricsService metricsService;

    public OrderController(OrderProcessingService orderProcessingService, MetricsService metricsService) {
        this.orderProcessingService = orderProcessingService;
        this.metricsService = metricsService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @RequestHeader(value = "Idempotency-Key", required = false) String headerIdempotencyKey,
            @Valid @RequestBody CreateOrderRequest request) {
        
        if (headerIdempotencyKey != null && !headerIdempotencyKey.isBlank()) {
            request.setIdempotencyKey(headerIdempotencyKey);
        }

        OrderResponse response = orderProcessingService.createOrder(request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<OrderResponse>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(orderProcessingService.getAllOrders(page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(orderProcessingService.getOrderById(id));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderProcessingService.cancelOrder(id));
    }

    @GetMapping("/stats")
    public ResponseEntity<SystemMetricsDTO> getOrderStats() {
        return ResponseEntity.ok(metricsService.getSystemMetrics());
    }
}
