package com.orderengine.service;

import com.orderengine.concurrency.OrderThreadPoolManager;
import com.orderengine.dto.CreateOrderRequest;
import com.orderengine.dto.OrderResponse;
import com.orderengine.entity.*;
import com.orderengine.inventory.AdaptiveInventoryGuardService;
import com.orderengine.inventory.InventoryReservationManager;
import com.orderengine.repository.*;
import com.orderengine.websocket.EventBroadcaster;
import com.orderengine.websocket.SystemEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class OrderProcessingService {

    private static final Logger log = LoggerFactory.getLogger(OrderProcessingService.class);

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryReservationRepository reservationRepository;
    private final IdempotencyKeyRepository idempotencyKeyRepository;
    private final OrderThreadPoolManager threadPoolManager;
    private final AdaptiveInventoryGuardService adaptiveGuard;
    private final InventoryReservationManager reservationManager;
    private final FailureSimulatorService failureSimulator;
    private final RetryService retryService;
    private final EventBroadcaster eventBroadcaster;

    public OrderProcessingService(OrderRepository orderRepository,
                                  ProductRepository productRepository,
                                  InventoryRepository inventoryRepository,
                                  InventoryReservationRepository reservationRepository,
                                  IdempotencyKeyRepository idempotencyKeyRepository,
                                  OrderThreadPoolManager threadPoolManager,
                                  AdaptiveInventoryGuardService adaptiveGuard,
                                  InventoryReservationManager reservationManager,
                                  FailureSimulatorService failureSimulator,
                                  RetryService retryService,
                                  EventBroadcaster eventBroadcaster) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
        this.reservationRepository = reservationRepository;
        this.idempotencyKeyRepository = idempotencyKeyRepository;
        this.threadPoolManager = threadPoolManager;
        this.adaptiveGuard = adaptiveGuard;
        this.reservationManager = reservationManager;
        this.failureSimulator = failureSimulator;
        this.retryService = retryService;
        this.eventBroadcaster = eventBroadcaster;
    }

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        if (request.getIdempotencyKey() != null && !request.getIdempotencyKey().isBlank()) {
            Optional<IdempotencyKey> existingKey = idempotencyKeyRepository.findByKeyValue(request.getIdempotencyKey());
            if (existingKey.isPresent()) {
                log.info("Idempotency hit! Returning cached order response for key: {}", request.getIdempotencyKey());
                Long orderId = existingKey.get().getOrderId();
                Order cachedOrder = orderRepository.findById(orderId)
                        .orElseThrow(() -> new IllegalStateException("Cached order not found"));
                return OrderResponse.fromEntity(cachedOrder);
            }
        }

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Product #" + request.getProductId() + " not found"));

        BigDecimal totalPrice = product.getPrice().multiply(BigDecimal.valueOf(request.getQuantity()));

        Order order = Order.builder()
                .customerId(request.getCustomerId())
                .status(OrderStatus.PENDING)
                .totalAmount(totalPrice)
                .idempotencyKey(request.getIdempotencyKey())
                .retryCount(0)
                .build();

        OrderItem item = OrderItem.builder()
                .order(order)
                .productId(product.getId())
                .quantity(request.getQuantity())
                .unitPrice(product.getPrice())
                .build();

        order.getItems().add(item);
        Order savedOrder = orderRepository.save(order);

        if (request.getIdempotencyKey() != null && !request.getIdempotencyKey().isBlank()) {
            IdempotencyKey keyRecord = IdempotencyKey.builder()
                    .keyValue(request.getIdempotencyKey())
                    .orderId(savedOrder.getId())
                    .statusCode(200)
                    .build();
            idempotencyKeyRepository.save(keyRecord);
        }

        eventBroadcaster.broadcast(SystemEvent.create("ORDER_CREATED", savedOrder.getId(), product.getId(), OrderResponse.fromEntity(savedOrder)));

        enqueueOrderTask(savedOrder.getId());

        return OrderResponse.fromEntity(savedOrder);
    }

    public void enqueueOrderTask(Long orderId) {
        threadPoolManager.submitOrderTask(orderId, () -> processOrderWorker(orderId));
    }

    public void processOrderWorker(Long orderId) {
        String workerName = Thread.currentThread().getName();
        log.info("[{}] Worker processing Order #{}", workerName, orderId);

        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) return;

        updateOrderStatus(orderId, OrderStatus.PROCESSING, workerName, null);
        eventBroadcaster.broadcast(SystemEvent.create("ORDER_PROCESSING", orderId, null, workerName));

        try {
            failureSimulator.maybeSimulateFailure(orderId);

            OrderItem item = order.getItems().get(0);
            Long productId = item.getProductId();
            Integer qty = item.getQuantity();

            boolean reserved = adaptiveGuard.executeGuardedReservation(productId,
                    () -> reservationManager.performAtomicReservation(orderId, productId, qty));

            if (reserved) {
                reservationManager.confirmOrder(orderId, productId, qty, workerName);
            } else {
                log.info("[{}] Stock unavailable for Order #{}. Marked OUT_OF_STOCK", workerName, orderId);
                updateOrderStatus(orderId, OrderStatus.OUT_OF_STOCK, workerName, "Insufficient inventory stock available");
                eventBroadcaster.broadcast(SystemEvent.create("ORDER_OUT_OF_STOCK", orderId, productId, "Out of Stock"));
            }

        } catch (Exception e) {
            log.error("[{}] Error executing Order #{}: {}", workerName, orderId, e.getMessage());
            retryService.handleOrderFailure(orderId, e);
        }
    }

    @Transactional
    public void updateOrderStatus(Long orderId, OrderStatus status, String workerId, String lastError) {
        orderRepository.findById(orderId).ifPresent(order -> {
            order.setStatus(status);
            if (workerId != null) order.setWorkerId(workerId);
            if (lastError != null) order.setLastError(lastError);
            orderRepository.save(order);
        });
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getAllOrders(int page, int size) {
        return orderRepository.findAllByOrderByIdDesc(PageRequest.of(page, size))
                .map(OrderResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order #" + id + " not found"));
        return OrderResponse.fromEntity(order);
    }

    @Transactional
    public OrderResponse cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order #" + orderId + " not found"));

        if (order.getStatus() == OrderStatus.CONFIRMED || order.getStatus() == OrderStatus.RESERVED) {
            reservationRepository.findByOrderId(orderId).ifPresent(res -> {
                if (res.getStatus() == ReservationStatus.ACTIVE || res.getStatus() == ReservationStatus.CONFIRMED) {
                    inventoryRepository.releaseReservationAtomic(res.getProductId(), res.getQuantity());
                    res.setStatus(ReservationStatus.RELEASED);
                    reservationRepository.save(res);
                }
            });
        }

        order.setStatus(OrderStatus.CANCELLED);
        Order saved = orderRepository.save(order);
        eventBroadcaster.broadcast(SystemEvent.create("ORDER_CANCELLED", orderId, null, "Cancelled by user"));
        return OrderResponse.fromEntity(saved);
    }
}
