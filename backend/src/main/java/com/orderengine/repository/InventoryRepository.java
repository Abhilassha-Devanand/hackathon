package com.orderengine.repository;

import com.orderengine.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    Optional<Inventory> findByProductId(Long productId);

    /**
     * ATOMIC INVENTORY CONCURRENCY CONTROL
     * Decreases available_quantity and increases reserved_quantity if and only if
     * available_quantity >= requested quantity.
     * Returns 1 if successful, 0 if stock was insufficient.
     */
    @Modifying
    @Query("UPDATE Inventory i SET i.availableQuantity = i.availableQuantity - :qty, " +
           "i.reservedQuantity = i.reservedQuantity + :qty, " +
           "i.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE i.productId = :productId AND i.availableQuantity >= :qty")
    int reserveInventoryAtomic(@Param("productId") Long productId, @Param("qty") Integer qty);

    /**
     * Confirms an active reservation by deducting from reserved_quantity.
     */
    @Modifying
    @Query("UPDATE Inventory i SET i.reservedQuantity = i.reservedQuantity - :qty, " +
           "i.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE i.productId = :productId AND i.reservedQuantity >= :qty")
    int confirmReservationAtomic(@Param("productId") Long productId, @Param("qty") Integer qty);

    /**
     * Releases an expired or cancelled reservation by moving stock back to available_quantity.
     */
    @Modifying
    @Query("UPDATE Inventory i SET i.availableQuantity = i.availableQuantity + :qty, " +
           "i.reservedQuantity = i.reservedQuantity - :qty, " +
           "i.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE i.productId = :productId AND i.reservedQuantity >= :qty")
    int releaseReservationAtomic(@Param("productId") Long productId, @Param("qty") Integer qty);
}
