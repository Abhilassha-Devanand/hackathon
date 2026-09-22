package com.orderengine.inventory;

import com.orderengine.entity.InventoryReservation;
import com.orderengine.entity.OrderStatus;
import com.orderengine.entity.ReservationStatus;
import com.orderengine.repository.InventoryRepository;
import com.orderengine.repository.InventoryReservationRepository;
import com.orderengine.repository.OrderRepository;
import com.orderengine.websocket.EventBroadcaster;
import com.orderengine.websocket.SystemEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class ReservationExpiryScheduler {

    private static final Logger log = LoggerFactory.getLogger(ReservationExpiryScheduler.class);

    private final InventoryReservationRepository reservationRepository;
    private final InventoryRepository inventoryRepository;
    private final OrderRepository orderRepository;
    private final EventBroadcaster eventBroadcaster;

    public ReservationExpiryScheduler(InventoryReservationRepository reservationRepository,
                                       InventoryRepository inventoryRepository,
                                       OrderRepository orderRepository,
                                       EventBroadcaster eventBroadcaster) {
        this.reservationRepository = reservationRepository;
        this.inventoryRepository = inventoryRepository;
        this.orderRepository = orderRepository;
        this.eventBroadcaster = eventBroadcaster;
    }

    @Scheduled(fixedRate = 5000)
    @Transactional
    public void processExpiredReservations() {
        LocalDateTime now = LocalDateTime.now();
        List<InventoryReservation> expiredList = reservationRepository.findExpiredReservations(ReservationStatus.ACTIVE, now);

        for (InventoryReservation reservation : expiredList) {
            log.info("Reservation #{} for Order #{} expired. Releasing stock (qty={})...",
                    reservation.getId(), reservation.getOrderId(), reservation.getQuantity());

            reservation.setStatus(ReservationStatus.EXPIRED);
            reservationRepository.save(reservation);

            inventoryRepository.releaseReservationAtomic(reservation.getProductId(), reservation.getQuantity());

            orderRepository.findById(reservation.getOrderId()).ifPresent(order -> {
                order.setStatus(OrderStatus.CANCELLED);
                order.setLastError("Reservation expired before payment confirmation");
                orderRepository.save(order);

                eventBroadcaster.broadcast(SystemEvent.create(
                        "ORDER_CANCELLED",
                        order.getId(),
                        reservation.getProductId(),
                        "Reservation expired and stock released"
                ));
            });
        }
    }
}
