package com.orderengine.inventory;

import com.orderengine.entity.InventoryReservation;
import com.orderengine.entity.Order;
import com.orderengine.entity.OrderStatus;
import com.orderengine.entity.ReservationStatus;
import com.orderengine.repository.InventoryRepository;
import com.orderengine.repository.InventoryReservationRepository;
import com.orderengine.repository.OrderRepository;
import com.orderengine.websocket.EventBroadcaster;
import com.orderengine.websocket.SystemEvent;
import com.orderengine.dto.OrderResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class InventoryReservationManager {

    private static final Logger log = LoggerFactory.getLogger(InventoryReservationManager.class);

    private final InventoryRepository inventoryRepository;
    private final InventoryReservationRepository reservationRepository;
    private final OrderRepository orderRepository;
    private final EventBroadcaster eventBroadcaster;

    public InventoryReservationManager(InventoryRepository inventoryRepository,
                                        InventoryReservationRepository reservationRepository,
                                        OrderRepository orderRepository,
                                        EventBroadcaster eventBroadcaster) {
        this.inventoryRepository = inventoryRepository;
        this.reservationRepository = reservationRepository;
        this.orderRepository = orderRepository;
        this.eventBroadcaster = eventBroadcaster;
    }

    /**
     * Executes atomic reservation inside a distinct Spring managed transaction.
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public boolean performAtomicReservation(Long orderId, Long productId, Integer qty) {
        int rowsUpdated = inventoryRepository.reserveInventoryAtomic(productId, qty);
        if (rowsUpdated > 0) {
            InventoryReservation reservation = InventoryReservation.builder()
                    .orderId(orderId)
                    .productId(productId)
                    .quantity(qty)
                    .status(ReservationStatus.ACTIVE)
                    .expiresAt(LocalDateTime.now().plusMinutes(2))
                    .build();
            reservationRepository.save(reservation);
            return true;
        }
        return false;
    }

    /**
     * Confirms the order and updates reservation inside a distinct Spring managed transaction.
     */
    @Transactional
    public void confirmOrder(Long orderId, Long productId, Integer qty, String workerName) {
        inventoryRepository.confirmReservationAtomic(productId, qty);

        reservationRepository.findByOrderId(orderId).ifPresent(res -> {
            res.setStatus(ReservationStatus.CONFIRMED);
            reservationRepository.save(res);
        });

        Order order = orderRepository.findById(orderId).orElseThrow();
        order.setStatus(OrderStatus.CONFIRMED);
        order.setWorkerId(workerName);
        order.setLastError(null);
        orderRepository.save(order);

        log.info("[{}] Order #{} successfully CONFIRMED for product #{} (qty={})", workerName, orderId, productId, qty);
        eventBroadcaster.broadcast(SystemEvent.create("ORDER_CONFIRMED", orderId, productId, OrderResponse.fromEntity(order)));
    }
}
