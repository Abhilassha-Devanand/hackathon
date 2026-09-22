package com.orderengine.repository;

import com.orderengine.entity.Order;
import com.orderengine.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByIdempotencyKey(String idempotencyKey);

    List<Order> findByStatus(OrderStatus status);

    Page<Order> findAllByOrderByIdDesc(Pageable pageable);

    long countByStatus(OrderStatus status);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.createdAt >= CURRENT_TIMESTAMP - 10 SECOND")
    long countOrdersInLastTenSeconds();
}
