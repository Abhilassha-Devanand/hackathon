package com.orderengine.concurrency;

import com.orderengine.dto.ThreadPoolMetricsDTO.WorkerStatusDTO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WorkerTracker {

    private final Map<String, WorkerStatus> workerMap = new ConcurrentHashMap<>();

    public static class WorkerStatus {
        private final String workerId;
        private volatile String state; // IDLE, PROCESSING
        private volatile Long currentOrderId;
        private volatile Long startedAtEpochMs;

        public WorkerStatus(String workerId) {
            this.workerId = workerId;
            this.state = "IDLE";
            this.currentOrderId = null;
            this.startedAtEpochMs = null;
        }

        public synchronized void setProcessing(Long orderId) {
            this.state = "PROCESSING";
            this.currentOrderId = orderId;
            this.startedAtEpochMs = System.currentTimeMillis();
        }

        public synchronized void setIdle() {
            this.state = "IDLE";
            this.currentOrderId = null;
            this.startedAtEpochMs = null;
        }

        public WorkerStatusDTO toDTO() {
            return WorkerStatusDTO.builder()
                    .workerId(workerId)
                    .status(state)
                    .currentOrderId(currentOrderId)
                    .startedAtEpochMs(startedAtEpochMs)
                    .build();
        }
    }

    public WorkerStatus getWorker(String workerId) {
        return workerMap.computeIfAbsent(workerId, WorkerStatus::new);
    }

    public List<WorkerStatusDTO> getAllWorkerStatuses(int poolSize) {
        List<WorkerStatusDTO> list = new ArrayList<>();
        for (int i = 1; i <= poolSize; i++) {
            String workerId = "Worker-" + i;
            WorkerStatus status = workerMap.computeIfAbsent(workerId, WorkerStatus::new);
            list.add(status.toDTO());
        }
        return list;
    }
}
