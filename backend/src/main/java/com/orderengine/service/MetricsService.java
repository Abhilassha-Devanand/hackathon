package com.orderengine.service;

import com.orderengine.concurrency.OrderThreadPoolManager;
import com.orderengine.dto.SystemMetricsDTO;
import com.orderengine.dto.ThreadPoolMetricsDTO;
import com.orderengine.entity.OrderStatus;
import com.orderengine.repository.DeadLetterQueueRepository;
import com.orderengine.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MetricsService {

    private final OrderRepository orderRepository;
    private final DeadLetterQueueRepository dlqRepository;
    private final OrderThreadPoolManager threadPoolManager;

    public MetricsService(OrderRepository orderRepository,
                          DeadLetterQueueRepository dlqRepository,
                          OrderThreadPoolManager threadPoolManager) {
        this.orderRepository = orderRepository;
        this.dlqRepository = dlqRepository;
        this.threadPoolManager = threadPoolManager;
    }

    @Transactional(readOnly = true)
    public SystemMetricsDTO getSystemMetrics() {
        long total = orderRepository.count();
        long confirmed = orderRepository.countByStatus(OrderStatus.CONFIRMED);
        long failed = orderRepository.countByStatus(OrderStatus.FAILED);
        long outOfStock = orderRepository.countByStatus(OrderStatus.OUT_OF_STOCK);
        long processing = orderRepository.countByStatus(OrderStatus.PROCESSING);
        long retrying = orderRepository.countByStatus(OrderStatus.RETRYING);
        long dlqCount = dlqRepository.count();

        ThreadPoolMetricsDTO poolMetrics = threadPoolManager.getMetrics();
        long ordersInLast10s = orderRepository.countOrdersInLastTenSeconds();
        double ordersPerSec = ordersInLast10s / 10.0;

        return SystemMetricsDTO.builder()
                .totalOrders(total)
                .successfulOrders(confirmed)
                .failedOrders(failed)
                .outOfStockOrders(outOfStock)
                .processingOrders(processing)
                .retryingOrders(retrying)
                .dlqOrders(dlqCount)
                .activeWorkers(poolMetrics.getActiveThreads())
                .queueSize(poolMetrics.getQueueSize())
                .ordersPerSecond(ordersPerSec)
                .avgProcessingTimeMs(poolMetrics.getAverageTaskDurationMs())
                .build();
    }
}
