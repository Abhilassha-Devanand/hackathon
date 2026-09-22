package com.orderengine.concurrency;

import com.orderengine.dto.ThreadPoolMetricsDTO;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class OrderThreadPoolManager {

    private static final Logger log = LoggerFactory.getLogger(OrderThreadPoolManager.class);

    @Value("${app.order.thread-pool-size:10}")
    private int initialPoolSize;

    private ThreadPoolExecutor executor;
    private BlockingQueue<Runnable> workQueue;
    private final WorkerTracker workerTracker;

    private final AtomicLong completedTasks = new AtomicLong(0);
    private final AtomicLong failedTasks = new AtomicLong(0);
    private final AtomicLong totalDurationMs = new AtomicLong(0);
    private final AtomicInteger threadIdCounter = new AtomicInteger(1);

    public OrderThreadPoolManager(WorkerTracker workerTracker) {
        this.workerTracker = workerTracker;
    }

    public ThreadPoolExecutor getExecutor() {
        return executor;
    }

    @PostConstruct
    public void init() {
        this.workQueue = new LinkedBlockingQueue<>();
        this.executor = new ThreadPoolExecutor(
                initialPoolSize,
                initialPoolSize,
                60L,
                TimeUnit.SECONDS,
                workQueue,
                new ThreadFactory() {
                    @Override
                    public Thread newThread(Runnable r) {
                        String threadName = "Worker-" + threadIdCounter.getAndIncrement();
                        Thread t = new Thread(r, threadName);
                        t.setDaemon(true);
                        return t;
                    }
                }
        );
        log.info("Initialized OrderThreadPoolManager with pool size: {}", initialPoolSize);
    }

    public synchronized void setPoolSize(int newSize) {
        if (newSize < 1) {
            throw new IllegalArgumentException("Thread pool size must be at least 1");
        }
        if (executor != null) {
            if (newSize > executor.getMaximumPoolSize()) {
                executor.setMaximumPoolSize(newSize);
                executor.setCorePoolSize(newSize);
            } else {
                executor.setCorePoolSize(newSize);
                executor.setMaximumPoolSize(newSize);
            }
            log.info("Updated ThreadPool size to: {}", newSize);
        }
    }

    public void submitOrderTask(Long orderId, Runnable task) {
        executor.submit(() -> {
            String workerName = Thread.currentThread().getName();
            WorkerTracker.WorkerStatus status = workerTracker.getWorker(workerName);
            status.setProcessing(orderId);
            long start = System.currentTimeMillis();

            try {
                task.run();
                completedTasks.incrementAndGet();
            } catch (Exception e) {
                failedTasks.incrementAndGet();
                log.error("Error executing order task #{} on worker {}: {}", orderId, workerName, e.getMessage(), e);
            } finally {
                long duration = System.currentTimeMillis() - start;
                totalDurationMs.addAndGet(duration);
                status.setIdle();
            }
        });
    }

    public ThreadPoolMetricsDTO getMetrics() {
        int poolSize = executor.getCorePoolSize();
        int active = executor.getActiveCount();
        int idle = poolSize - active;
        if (idle < 0) idle = 0;
        int queueSize = workQueue.size();
        long completed = completedTasks.get();
        long failed = failedTasks.get();
        long totalCount = completed + failed;
        double avgDuration = totalCount > 0 ? (double) totalDurationMs.get() / totalCount : 0.0;

        return ThreadPoolMetricsDTO.builder()
                .corePoolSize(poolSize)
                .maximumPoolSize(executor.getMaximumPoolSize())
                .activeThreads(active)
                .idleThreads(idle)
                .queueSize(queueSize)
                .completedTaskCount(completed)
                .failedTaskCount(failed)
                .averageTaskDurationMs(avgDuration)
                .workers(workerTracker.getAllWorkerStatuses(poolSize))
                .build();
    }

    @PreDestroy
    public void shutdown() {
        if (executor != null) {
            executor.shutdown();
        }
    }
}
