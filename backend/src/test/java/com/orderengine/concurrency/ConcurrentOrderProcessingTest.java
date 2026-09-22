package com.orderengine.concurrency;

import com.orderengine.dto.CreateOrderRequest;
import com.orderengine.entity.Inventory;
import com.orderengine.entity.Order;
import com.orderengine.entity.OrderStatus;
import com.orderengine.repository.InventoryRepository;
import com.orderengine.repository.OrderRepository;
import com.orderengine.service.InventoryService;
import com.orderengine.service.OrderProcessingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class ConcurrentOrderProcessingTest {

    @Autowired
    private OrderProcessingService orderProcessingService;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Test
    @DisplayName("100 Concurrent Orders competing for 1 stock item -> Exactly 1 Success, 99 Out-Of-Stock, Stock = 0")
    public void testLastItemRaceCondition() throws Exception {
        Long productId = 1L; // GPU
        int initialStock = 1;

        // Set initial stock to 1
        inventoryService.setInventoryStock(productId, initialStock);

        int totalOrders = 100;
        int threadPoolSize = 20;

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(totalOrders);
        ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);

        for (int i = 1; i <= totalOrders; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    startLatch.await(); // Synchronize all 100 threads to fire at exact same millisecond
                    CreateOrderRequest req = CreateOrderRequest.builder()
                            .customerId("CUST-RACE-" + index)
                            .productId(productId)
                            .quantity(1)
                            .idempotencyKey("ORD-KEY-" + UUID.randomUUID())
                            .build();
                    orderProcessingService.createOrder(req);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    finishLatch.countDown();
                }
            });
        }

        // Fire all 100 orders simultaneously!
        startLatch.countDown();

        // Wait up to 15 seconds for all order worker tasks to finish
        boolean completed = finishLatch.await(15, TimeUnit.SECONDS);
        assertTrue(completed, "All 100 concurrent orders should complete processing within 15 seconds");

        // Wait a short buffer for final DB commits
        Thread.sleep(1000);

        // Assertions
        Inventory finalInventory = inventoryRepository.findByProductId(productId).orElseThrow();
        long confirmedCount = orderRepository.countByStatus(OrderStatus.CONFIRMED);
        long outOfStockCount = orderRepository.countByStatus(OrderStatus.OUT_OF_STOCK);

        System.out.println("=== CONCURRENCY TEST RESULTS ===");
        System.out.println("Total Attempted Orders: " + totalOrders);
        System.out.println("Confirmed Orders: " + confirmedCount);
        System.out.println("Out of Stock Orders: " + outOfStockCount);
        System.out.println("Final Available Stock: " + finalInventory.getAvailableQuantity());
        System.out.println("Final Reserved Stock: " + finalInventory.getReservedQuantity());

        // CRITICAL INVENTORY SAFETY CHECKS
        assertEquals(1, confirmedCount, "STRICT REQUIREMENT: Exactly 1 order must be CONFIRMED");
        assertEquals(99, outOfStockCount, "STRICT REQUIREMENT: Exactly 99 orders must be OUT_OF_STOCK");
        assertEquals(0, finalInventory.getAvailableQuantity(), "STRICT REQUIREMENT: Available inventory must be exactly 0 (NEVER NEGATIVE)");
        assertTrue(finalInventory.getAvailableQuantity() >= 0, "INVARIANT GUARANTEE: Available stock can NEVER be negative");

        executor.shutdown();
    }
}
