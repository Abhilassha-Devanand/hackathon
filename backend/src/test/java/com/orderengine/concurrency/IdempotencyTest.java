package com.orderengine.concurrency;

import com.orderengine.dto.CreateOrderRequest;
import com.orderengine.dto.OrderResponse;
import com.orderengine.entity.Order;
import com.orderengine.repository.OrderRepository;
import com.orderengine.service.OrderProcessingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class IdempotencyTest {

    @Autowired
    private OrderProcessingService orderProcessingService;

    @Autowired
    private OrderRepository orderRepository;

    @Test
    @DisplayName("Duplicate request with same Idempotency-Key returns identical order and creates only 1 record")
    public void testIdempotentDuplicateRequests() throws Exception {
        String sameKey = "IDEM-KEY-UNIQUE-999";
        int repeatCount = 5;

        CountDownLatch latch = new CountDownLatch(repeatCount);
        ExecutorService executor = Executors.newFixedThreadPool(repeatCount);

        for (int i = 0; i < repeatCount; i++) {
            executor.submit(() -> {
                try {
                    CreateOrderRequest req = CreateOrderRequest.builder()
                            .customerId("CUST-IDEM-1")
                            .productId(2L) // MacBook
                            .quantity(1)
                            .idempotencyKey(sameKey)
                            .build();
                    orderProcessingService.createOrder(req);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        // Check database
        long orderCountWithKey = orderRepository.findAll().stream()
                .filter(o -> sameKey.equals(o.getIdempotencyKey()))
                .count();

        assertEquals(1, orderCountWithKey, "Only 1 order record must be created for duplicate idempotency key");
    }
}
