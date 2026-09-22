package com.orderengine.concurrency;

import com.orderengine.dto.CreateOrderRequest;
import com.orderengine.dto.FailureSimulationConfig;

import com.orderengine.entity.OrderStatus;
import com.orderengine.repository.DeadLetterQueueRepository;
import com.orderengine.repository.OrderRepository;
import com.orderengine.service.FailureSimulatorService;
import com.orderengine.service.OrderProcessingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class RetryAndDlqTest {

    @Autowired
    private OrderProcessingService orderProcessingService;

    @Autowired
    private FailureSimulatorService failureSimulatorService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private DeadLetterQueueRepository dlqRepository;

    @Test
    @DisplayName("Orders failing 3 consecutive times are moved to Dead Letter Queue (DLQ)")
    public void testBoundedRetryAndDlqRouting() throws Exception {
        // Enable 100% transient failure rate
        failureSimulatorService.updateConfig(FailureSimulationConfig.builder()
                .enabled(true)
                .failureRatePercentage(100.0)
                .transientFailuresOnly(true)
                .build());

        CreateOrderRequest req = CreateOrderRequest.builder()
                .customerId("CUST-FAIL-1")
                .productId(3L) // PS5
                .quantity(1)
                .idempotencyKey("DLQ-TEST-KEY-1")
                .build();

        var response = orderProcessingService.createOrder(req);
        Long orderId = response.getId();

        // Wait up to 8 seconds for retry backoffs (500ms + 1000ms + 2000ms) to exhaust
        Thread.sleep(7000);

        var dlqEntry = dlqRepository.findByOrderId(orderId);
        assertTrue(dlqEntry.isPresent(), "Order should be present in Dead Letter Queue after max retries");
        assertEquals(3, dlqEntry.get().getRetryCount(), "Retry count in DLQ should be 3");

        var finalOrder = orderRepository.findById(orderId).orElseThrow();
        assertEquals(OrderStatus.DLQ, finalOrder.getStatus(), "Order status in DB should be DLQ");
    }
}
