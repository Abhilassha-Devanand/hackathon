package com.orderengine.controller;

import com.orderengine.concurrency.OrderThreadPoolManager;
import com.orderengine.dto.ConcurrentTestRequest;
import com.orderengine.dto.ConcurrentTestResponse;
import com.orderengine.dto.CreateOrderRequest;
import com.orderengine.dto.FailureSimulationConfig;
import com.orderengine.entity.Inventory;
import com.orderengine.entity.OrderStatus;
import com.orderengine.repository.InventoryRepository;
import com.orderengine.repository.OrderRepository;
import com.orderengine.service.FailureSimulatorService;
import com.orderengine.service.InventoryService;
import com.orderengine.service.OrderProcessingService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/test")
@CrossOrigin(origins = "*")
public class ConcurrencyTestController {

    private static final Logger log = LoggerFactory.getLogger(ConcurrencyTestController.class);

    private final OrderProcessingService orderProcessingService;
    private final InventoryRepository inventoryRepository;
    private final OrderRepository orderRepository;
    private final InventoryService inventoryService;
    private final OrderThreadPoolManager threadPoolManager;
    private final FailureSimulatorService failureSimulatorService;

    public ConcurrencyTestController(OrderProcessingService orderProcessingService,
                                      InventoryRepository inventoryRepository,
                                      OrderRepository orderRepository,
                                      InventoryService inventoryService,
                                      OrderThreadPoolManager threadPoolManager,
                                      FailureSimulatorService failureSimulatorService) {
        this.orderProcessingService = orderProcessingService;
        this.inventoryRepository = inventoryRepository;
        this.orderRepository = orderRepository;
        this.inventoryService = inventoryService;
        this.threadPoolManager = threadPoolManager;
        this.failureSimulatorService = failureSimulatorService;
    }

    @PostMapping("/concurrent-orders")
    public ResponseEntity<ConcurrentTestResponse> runConcurrentOrdersTest(@Valid @RequestBody ConcurrentTestRequest request) {
        log.info("=== STARTING CONCURRENCY TEST: {} orders for Product #{} ===", request.getNumberOfOrders(), request.getProductId());

        if (request.getThreadPoolSize() != null && request.getThreadPoolSize() > 0) {
            threadPoolManager.setPoolSize(request.getThreadPoolSize());
        }

        if (request.getInitialStockOverride() != null && request.getInitialStockOverride() >= 0) {
            inventoryService.setInventoryStock(request.getProductId(), request.getInitialStockOverride());
        }

        if (request.getFailureSimulationPercentage() != null && request.getFailureSimulationPercentage() > 0) {
            failureSimulatorService.updateConfig(FailureSimulationConfig.builder()
                    .enabled(true)
                    .failureRatePercentage(request.getFailureSimulationPercentage())
                    .transientFailuresOnly(true)
                    .build());
        }

        Inventory initialInventory = inventoryRepository.findByProductId(request.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Product #" + request.getProductId() + " not found"));
        int initialStock = initialInventory.getAvailableQuantity();

        int totalOrders = request.getNumberOfOrders();
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(totalOrders);

        ExecutorService clientSimulatorPool = Executors.newFixedThreadPool(Math.min(totalOrders, 50));
        long startTime = System.currentTimeMillis();

        for (int i = 1; i <= totalOrders; i++) {
            final int index = i;
            clientSimulatorPool.submit(() -> {
                try {
                    startLatch.await();
                    CreateOrderRequest req = CreateOrderRequest.builder()
                            .customerId("SIM-CUST-" + index)
                            .productId(request.getProductId())
                            .quantity(request.getQuantityPerOrder())
                            .idempotencyKey("SIM-ORD-" + UUID.randomUUID())
                            .build();
                    orderProcessingService.createOrder(req);
                } catch (Exception e) {
                    log.error("Error submitting test order {}: {}", index, e.getMessage());
                } finally {
                    finishLatch.countDown();
                }
            });
        }

        startLatch.countDown();

        try {
            finishLatch.await(30, TimeUnit.SECONDS);
            Thread.sleep(1000);
        } catch (InterruptedException ignored) {}

        clientSimulatorPool.shutdown();
        long duration = System.currentTimeMillis() - startTime;

        Inventory finalInventory = inventoryRepository.findByProductId(request.getProductId()).orElseThrow();
        int finalStock = finalInventory.getAvailableQuantity();

        long confirmedCount = orderRepository.countByStatus(OrderStatus.CONFIRMED);
        long outOfStockCount = orderRepository.countByStatus(OrderStatus.OUT_OF_STOCK);
        long failedCount = orderRepository.countByStatus(OrderStatus.FAILED);
        long dlqCount = orderRepository.countByStatus(OrderStatus.DLQ);

        double peakReqSec = totalOrders / Math.max((duration / 1000.0), 0.1);
        boolean safetyVerified = finalStock >= 0 && finalStock <= initialStock;

        String summary = String.format("Simulated %d concurrent orders against initial stock of %d. Result: %d CONFIRMED, %d OUT_OF_STOCK. Final Stock = %d (Never Negative: %s)",
                totalOrders, initialStock, confirmedCount, outOfStockCount, finalStock, safetyVerified ? "PASSED" : "FAILED");

        ConcurrentTestResponse response = ConcurrentTestResponse.builder()
                .testName("Concurrent Order Execution")
                .productId(request.getProductId())
                .initialStock(initialStock)
                .finalStock(finalStock)
                .totalAttempted(totalOrders)
                .successful((int) confirmedCount)
                .outOfStock((int) outOfStockCount)
                .failed((int) failedCount)
                .sentToDlq((int) dlqCount)
                .executionDurationMs(duration)
                .peakOrdersPerSecond(peakReqSec)
                .inventorySafetyVerified(safetyVerified)
                .message(summary)
                .build();

        log.info("=== CONCURRENCY TEST COMPLETED: {} ===", summary);
        return ResponseEntity.ok(response);
    }
}
