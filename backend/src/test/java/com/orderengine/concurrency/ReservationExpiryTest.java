package com.orderengine.concurrency;

import com.orderengine.entity.InventoryReservation;
import com.orderengine.entity.ReservationStatus;
import com.orderengine.inventory.ReservationExpiryScheduler;
import com.orderengine.repository.InventoryRepository;
import com.orderengine.repository.InventoryReservationRepository;
import com.orderengine.service.InventoryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class ReservationExpiryTest {

    @Autowired
    private InventoryReservationRepository reservationRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private ReservationExpiryScheduler expiryScheduler;

    @Test
    @Transactional
    @DisplayName("Expired inventory reservations automatically release stock back to available quantity")
    public void testReservationExpiryAndStockRelease() {
        Long productId = 1L;
        inventoryService.setInventoryStock(productId, 10);

        // Reserve 2 items atomically
        inventoryRepository.reserveInventoryAtomic(productId, 2);

        // Manually save an ACTIVE reservation that expired 5 minutes ago
        InventoryReservation expiredReservation = InventoryReservation.builder()
                .orderId(999L)
                .productId(productId)
                .quantity(2)
                .status(ReservationStatus.ACTIVE)
                .expiresAt(LocalDateTime.now().minusMinutes(5))
                .build();
        reservationRepository.save(expiredReservation);

        // Trigger background expiry scheduler
        expiryScheduler.processExpiredReservations();

        // Verify reservation marked EXPIRED
        InventoryReservation updatedRes = reservationRepository.findById(expiredReservation.getId()).orElseThrow();
        assertEquals(ReservationStatus.EXPIRED, updatedRes.getStatus());

        // Verify stock returned (Available stock should be back to 10)
        var inv = inventoryRepository.findByProductId(productId).orElseThrow();
        assertEquals(10, inv.getAvailableQuantity(), "Available stock should return to 10 after expired reservation release");
    }
}
