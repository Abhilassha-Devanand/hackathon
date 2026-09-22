package com.orderengine.repository;

import com.orderengine.entity.RetryRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RetryRecordRepository extends JpaRepository<RetryRecord, Long> {
    List<RetryRecord> findByOrderIdOrderByAttemptNumberAsc(Long orderId);
}
