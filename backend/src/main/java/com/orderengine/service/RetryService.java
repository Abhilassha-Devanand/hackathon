package com.orderengine.service;

import com.orderengine.entity.Order;
import com.orderengine.entity.OrderStatus;
import com.orderengine.entity.RetryRecord;
import com.orderengine.repository.OrderRepository;
import com.orderengine.repository.RetryRecordRepository;
import com.orderengine.websocket.EventBroadcaster;
import com.orderengine.websocket.SystemEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class RetryService {

    private static final Logger log = LoggerFactory.getLogger(RetryService.class);

    public static final int MAX_RETRIES = 3;

    private final OrderRepository orderRepository;
    private final RetryRecordRepository retryRecordRepository;
    private final DeadLetterQueueService dlqService;
    private final EventBroadcaster eventBroadcaster;
    private final OrderProcessingService orderProcessingService;

    private final ScheduledExecutorService retryScheduler = Executors.newScheduledThreadPool(4);

    public RetryService(OrderRepository orderRepository,
                        RetryRecordRepository retryRecordRepository,
                        DeadLetterQueueService dlqService,
                        EventBroadcaster eventBroadcaster,
                        @Lazy OrderProcessingService orderProcessingService) {
        this.orderRepository = orderRepository;
        this.retryRecordRepository = retryRecordRepository;
        this.dlqService = dlqService;
        this.eventBroadcaster = eventBroadcaster;
        this.orderProcessingService = orderProcessingService;
    }

    public void handleOrderFailure(Long orderId, Throwable error) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) return;

        String errorMessage = error.getMessage() != null ? error.getMessage() : error.getClass().getSimpleName();

        if (isPermanentError(error)) {
            log.warn("Order #{} failed with PERMANENT non-retryable error: {}", orderId, errorMessage);
            order.setStatus(OrderStatus.FAILED);
            order.setLastError(errorMessage);
            orderRepository.save(order);
            eventBroadcaster.broadcast(SystemEvent.create("ORDER_FAILED", orderId, null, errorMessage));
            return;
        }

        int currentRetry = order.getRetryCount() + 1;
        if (currentRetry > MAX_RETRIES) {
            log.warn("Order #{} exceeded maximum retries ({}/{}). Forwarding to DLQ.", orderId, order.getRetryCount(), MAX_RETRIES);
            dlqService.sendToDlq(order, "Exceeded maximum retry limit of " + MAX_RETRIES, errorMessage);
            return;
        }

        long backoffDelayMs = 500L * (1L << (currentRetry - 1));
        log.info("Scheduling Retry #{}/{} for Order #{} in {}ms", currentRetry, MAX_RETRIES, orderId, backoffDelayMs);

        order.setRetryCount(currentRetry);
        order.setStatus(OrderStatus.RETRYING);
        order.setLastError("Attempt " + currentRetry + " failed: " + errorMessage);
        orderRepository.save(order);

        RetryRecord record = RetryRecord.builder()
                .orderId(orderId)
                .attemptNumber(currentRetry)
                .errorMessage(errorMessage)
                .nextRetryAt(LocalDateTime.now().plusNanos(backoffDelayMs * 1_000_000))
                .build();
        retryRecordRepository.save(record);

        eventBroadcaster.broadcast(SystemEvent.create("ORDER_RETRYING", orderId, null, "Retry attempt " + currentRetry + " scheduled in " + backoffDelayMs + "ms"));

        retryScheduler.schedule(() -> {
            orderProcessingService.enqueueOrderTask(orderId);
        }, backoffDelayMs, TimeUnit.MILLISECONDS);
    }

    private boolean isPermanentError(Throwable error) {
        return error instanceof IllegalArgumentException
                || error instanceof NullPointerException
                || (error.getMessage() != null && error.getMessage().toLowerCase().contains("permanent"));
    }
}
