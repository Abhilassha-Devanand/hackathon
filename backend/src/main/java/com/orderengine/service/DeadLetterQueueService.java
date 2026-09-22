package com.orderengine.service;

import com.orderengine.dto.DlqResponse;
import com.orderengine.entity.DeadLetterQueue;
import com.orderengine.entity.Order;
import com.orderengine.entity.OrderStatus;
import com.orderengine.repository.DeadLetterQueueRepository;
import com.orderengine.repository.OrderRepository;
import com.orderengine.websocket.EventBroadcaster;
import com.orderengine.websocket.SystemEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DeadLetterQueueService {

    private static final Logger log = LoggerFactory.getLogger(DeadLetterQueueService.class);

    private final DeadLetterQueueRepository dlqRepository;
    private final OrderRepository orderRepository;
    private final EventBroadcaster eventBroadcaster;
    private final OrderProcessingService orderProcessingService;

    public DeadLetterQueueService(DeadLetterQueueRepository dlqRepository,
                                  OrderRepository orderRepository,
                                  EventBroadcaster eventBroadcaster,
                                  @Lazy OrderProcessingService orderProcessingService) {
        this.dlqRepository = dlqRepository;
        this.orderRepository = orderRepository;
        this.eventBroadcaster = eventBroadcaster;
        this.orderProcessingService = orderProcessingService;
    }

    @Transactional
    public void sendToDlq(Order order, String failureReason, String lastError) {
        log.warn("Moving Order #{} to Dead Letter Queue. Reason: {}", order.getId(), failureReason);

        order.setStatus(OrderStatus.DLQ);
        order.setLastError(lastError);
        orderRepository.save(order);

        DeadLetterQueue dlq = DeadLetterQueue.builder()
                .orderId(order.getId())
                .failureReason(failureReason)
                .retryCount(order.getRetryCount())
                .payload("CustomerId: " + order.getCustomerId() + ", Amount: " + order.getTotalAmount())
                .lastError(lastError)
                .build();
        dlqRepository.save(dlq);

        eventBroadcaster.broadcast(SystemEvent.create("ORDER_SENT_TO_DLQ", order.getId(), null, DlqResponse.fromEntity(dlq)));
    }

    public List<DlqResponse> getAllDlqEntries() {
        return dlqRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(DlqResponse::fromEntity)
                .toList();
    }

    @Transactional
    public void retryDlqOrder(Long dlqId) {
        DeadLetterQueue dlq = dlqRepository.findById(dlqId)
                .orElseThrow(() -> new IllegalArgumentException("DLQ Entry #" + dlqId + " not found"));

        Order order = orderRepository.findById(dlq.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("Order #" + dlq.getOrderId() + " not found"));

        log.info("Administrator retrying DLQ Order #{}", order.getId());

        dlqRepository.delete(dlq);

        order.setRetryCount(0);
        order.setStatus(OrderStatus.PENDING);
        order.setLastError(null);
        orderRepository.save(order);

        orderProcessingService.enqueueOrderTask(order.getId());
    }
}
