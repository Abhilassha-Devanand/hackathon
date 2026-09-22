package com.orderengine.repository;

import com.orderengine.entity.InventoryReservation;
import com.orderengine.entity.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryReservationRepository extends JpaRepository<InventoryReservation, Long> {

    Optional<InventoryReservation> findByOrderId(Long orderId);

    List<InventoryReservation> findByStatus(ReservationStatus status);

    @Query("SELECT r FROM InventoryReservation r WHERE r.status = :status AND r.expiresAt <= :now")
    List<InventoryReservation> findExpiredReservations(
            @Param("status") ReservationStatus status,
            @Param("now") LocalDateTime now
    );
}
