package com.orderengine.inventory;

import com.orderengine.entity.ContentionLevel;
import com.orderengine.entity.Inventory;
import com.orderengine.repository.InventoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

@Service
public class AdaptiveInventoryGuardService {

    private static final Logger log = LoggerFactory.getLogger(AdaptiveInventoryGuardService.class);

    private final InventoryRepository inventoryRepository;

    private final Map<Long, ReentrantLock> productLocks = new ConcurrentHashMap<>();
    private final Map<Long, Queue<Long>> productRequestTimestamps = new ConcurrentHashMap<>();
    private final Map<Long, AtomicLong> successfulAllocationsMap = new ConcurrentHashMap<>();
    private final Map<Long, AtomicLong> failedAllocationsMap = new ConcurrentHashMap<>();

    public AdaptiveInventoryGuardService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    public boolean executeGuardedReservation(Long productId, Supplier<Boolean> reservationTask) {
        recordRequest(productId);
        ContentionLevel level = evaluateContentionLevel(productId);

        if (level == ContentionLevel.HIGH) {
            ReentrantLock lock = productLocks.computeIfAbsent(productId, k -> new ReentrantLock());
            lock.lock();
            try {
                log.info("AdaptiveGuard [HIGH CONTENTION]: Serializing inventory attempt for Product #{}", productId);
                boolean success = reservationTask.get();
                recordResult(productId, success);
                return success;
            } finally {
                lock.unlock();
            }
        } else {
            boolean success = reservationTask.get();
            recordResult(productId, success);
            return success;
        }
    }

    private void recordRequest(Long productId) {
        long now = System.currentTimeMillis();
        Queue<Long> timestamps = productRequestTimestamps.computeIfAbsent(productId, k -> new ConcurrentLinkedQueue<>());
        timestamps.add(now);
        cleanOldTimestamps(timestamps, now);
    }

    private void recordResult(Long productId, boolean success) {
        if (success) {
            successfulAllocationsMap.computeIfAbsent(productId, k -> new AtomicLong(0)).incrementAndGet();
        } else {
            failedAllocationsMap.computeIfAbsent(productId, k -> new AtomicLong(0)).incrementAndGet();
        }
    }

    public ContentionLevel evaluateContentionLevel(Long productId) {
        Queue<Long> timestamps = productRequestTimestamps.computeIfAbsent(productId, k -> new ConcurrentLinkedQueue<>());
        long now = System.currentTimeMillis();
        cleanOldTimestamps(timestamps, now);

        double reqPerSec = timestamps.size() / 10.0;

        Inventory inventory = inventoryRepository.findByProductId(productId).orElse(null);
        int availableStock = (inventory != null) ? inventory.getAvailableQuantity() : 0;

        if (availableStock <= 3 || reqPerSec >= 15.0) {
            return ContentionLevel.HIGH;
        } else if (availableStock <= 15 || reqPerSec >= 5.0) {
            return ContentionLevel.MEDIUM;
        } else {
            return ContentionLevel.LOW;
        }
    }

    public double getRequestsPerSecond(Long productId) {
        Queue<Long> timestamps = productRequestTimestamps.get(productId);
        if (timestamps == null) return 0.0;
        long now = System.currentTimeMillis();
        cleanOldTimestamps(timestamps, now);
        return timestamps.size() / 10.0;
    }

    public long getSuccessfulAllocations(Long productId) {
        AtomicLong counter = successfulAllocationsMap.get(productId);
        return counter != null ? counter.get() : 0L;
    }

    public long getFailedAllocations(Long productId) {
        AtomicLong counter = failedAllocationsMap.get(productId);
        return counter != null ? counter.get() : 0L;
    }

    private void cleanOldTimestamps(Queue<Long> timestamps, long now) {
        long cutoff = now - 10000;
        while (!timestamps.isEmpty() && timestamps.peek() < cutoff) {
            timestamps.poll();
        }
    }
}
