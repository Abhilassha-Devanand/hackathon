package com.orderengine.dto;

import java.util.List;

public class ThreadPoolMetricsDTO {
    private int corePoolSize;
    private int maximumPoolSize;
    private int activeThreads;
    private int idleThreads;
    private int queueSize;
    private long completedTaskCount;
    private long failedTaskCount;
    private double averageTaskDurationMs;
    private List<WorkerStatusDTO> workers;

    public ThreadPoolMetricsDTO() {}

    public ThreadPoolMetricsDTO(int corePoolSize, int maximumPoolSize, int activeThreads, int idleThreads,
                                int queueSize, long completedTaskCount, long failedTaskCount,
                                double averageTaskDurationMs, List<WorkerStatusDTO> workers) {
        this.corePoolSize = corePoolSize;
        this.maximumPoolSize = maximumPoolSize;
        this.activeThreads = activeThreads;
        this.idleThreads = idleThreads;
        this.queueSize = queueSize;
        this.completedTaskCount = completedTaskCount;
        this.failedTaskCount = failedTaskCount;
        this.averageTaskDurationMs = averageTaskDurationMs;
        this.workers = workers;
    }

    public int getCorePoolSize() { return corePoolSize; }
    public void setCorePoolSize(int corePoolSize) { this.corePoolSize = corePoolSize; }

    public int getMaximumPoolSize() { return maximumPoolSize; }
    public void setMaximumPoolSize(int maximumPoolSize) { this.maximumPoolSize = maximumPoolSize; }

    public int getActiveThreads() { return activeThreads; }
    public void setActiveThreads(int activeThreads) { this.activeThreads = activeThreads; }

    public int getIdleThreads() { return idleThreads; }
    public void setIdleThreads(int idleThreads) { this.idleThreads = idleThreads; }

    public int getQueueSize() { return queueSize; }
    public void setQueueSize(int queueSize) { this.queueSize = queueSize; }

    public long getCompletedTaskCount() { return completedTaskCount; }
    public void setCompletedTaskCount(long completedTaskCount) { this.completedTaskCount = completedTaskCount; }

    public long getFailedTaskCount() { return failedTaskCount; }
    public void setFailedTaskCount(long failedTaskCount) { this.failedTaskCount = failedTaskCount; }

    public double getAverageTaskDurationMs() { return averageTaskDurationMs; }
    public void setAverageTaskDurationMs(double averageTaskDurationMs) { this.averageTaskDurationMs = averageTaskDurationMs; }

    public List<WorkerStatusDTO> getWorkers() { return workers; }
    public void setWorkers(List<WorkerStatusDTO> workers) { this.workers = workers; }

    public static class WorkerStatusDTO {
        private String workerId;
        private String status;
        private Long currentOrderId;
        private Long startedAtEpochMs;

        public WorkerStatusDTO() {}

        public WorkerStatusDTO(String workerId, String status, Long currentOrderId, Long startedAtEpochMs) {
            this.workerId = workerId;
            this.status = status;
            this.currentOrderId = currentOrderId;
            this.startedAtEpochMs = startedAtEpochMs;
        }

        public String getWorkerId() { return workerId; }
        public void setWorkerId(String workerId) { this.workerId = workerId; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public Long getCurrentOrderId() { return currentOrderId; }
        public void setCurrentOrderId(Long currentOrderId) { this.currentOrderId = currentOrderId; }

        public Long getStartedAtEpochMs() { return startedAtEpochMs; }
        public void setStartedAtEpochMs(Long startedAtEpochMs) { this.startedAtEpochMs = startedAtEpochMs; }

        public static Builder builder() { return new Builder(); }

        public static class Builder {
            private String workerId;
            private String status;
            private Long currentOrderId;
            private Long startedAtEpochMs;

            public Builder workerId(String workerId) { this.workerId = workerId; return this; }
            public Builder status(String status) { this.status = status; return this; }
            public Builder currentOrderId(Long currentOrderId) { this.currentOrderId = currentOrderId; return this; }
            public Builder startedAtEpochMs(Long startedAtEpochMs) { this.startedAtEpochMs = startedAtEpochMs; return this; }

            public WorkerStatusDTO build() {
                return new WorkerStatusDTO(workerId, status, currentOrderId, startedAtEpochMs);
            }
        }
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private int corePoolSize;
        private int maximumPoolSize;
        private int activeThreads;
        private int idleThreads;
        private int queueSize;
        private long completedTaskCount;
        private long failedTaskCount;
        private double averageTaskDurationMs;
        private List<WorkerStatusDTO> workers;

        public Builder corePoolSize(int corePoolSize) { this.corePoolSize = corePoolSize; return this; }
        public Builder maximumPoolSize(int maximumPoolSize) { this.maximumPoolSize = maximumPoolSize; return this; }
        public Builder activeThreads(int activeThreads) { this.activeThreads = activeThreads; return this; }
        public Builder idleThreads(int idleThreads) { this.idleThreads = idleThreads; return this; }
        public Builder queueSize(int queueSize) { this.queueSize = queueSize; return this; }
        public Builder completedTaskCount(long completedTaskCount) { this.completedTaskCount = completedTaskCount; return this; }
        public Builder failedTaskCount(long failedTaskCount) { this.failedTaskCount = failedTaskCount; return this; }
        public Builder averageTaskDurationMs(double averageTaskDurationMs) { this.averageTaskDurationMs = averageTaskDurationMs; return this; }
        public Builder workers(List<WorkerStatusDTO> workers) { this.workers = workers; return this; }

        public ThreadPoolMetricsDTO build() {
            return new ThreadPoolMetricsDTO(corePoolSize, maximumPoolSize, activeThreads, idleThreads, queueSize, completedTaskCount, failedTaskCount, averageTaskDurationMs, workers);
        }
    }
}
