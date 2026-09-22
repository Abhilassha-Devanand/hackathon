package com.orderengine.repository;

import com.orderengine.entity.DeadLetterQueue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeadLetterQueueRepository extends JpaRepository<DeadLetterQueue, Long> {
    Optional<DeadLetterQueue> findByOrderId(Long orderId);
    List<DeadLetterQueue> findAllByOrderByCreatedAtDesc();
}
