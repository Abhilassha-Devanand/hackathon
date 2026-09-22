package com.orderengine.dto;

public class SystemMetricsDTO {
    private long totalOrders;
    private long successfulOrders;
    private long failedOrders;
    private long outOfStockOrders;
    private long processingOrders;
    private long retryingOrders;
    private long dlqOrders;
    private int activeWorkers;
    private int queueSize;
    private double ordersPerSecond;
    private double avgProcessingTimeMs;

    public SystemMetricsDTO() {}

    public SystemMetricsDTO(long totalOrders, long successfulOrders, long failedOrders, long outOfStockOrders,
                            long processingOrders, long retryingOrders, long dlqOrders, int activeWorkers,
                            int queueSize, double ordersPerSecond, double avgProcessingTimeMs) {
        this.totalOrders = totalOrders;
        this.successfulOrders = successfulOrders;
        this.failedOrders = failedOrders;
        this.outOfStockOrders = outOfStockOrders;
        this.processingOrders = processingOrders;
        this.retryingOrders = retryingOrders;
        this.dlqOrders = dlqOrders;
        this.activeWorkers = activeWorkers;
        this.queueSize = queueSize;
        this.ordersPerSecond = ordersPerSecond;
        this.avgProcessingTimeMs = avgProcessingTimeMs;
    }

    public long getTotalOrders() { return totalOrders; }
    public void setTotalOrders(long totalOrders) { this.totalOrders = totalOrders; }

    public long getSuccessfulOrders() { return successfulOrders; }
    public void setSuccessfulOrders(long successfulOrders) { this.successfulOrders = successfulOrders; }

    public long getFailedOrders() { return failedOrders; }
    public void setFailedOrders(long failedOrders) { this.failedOrders = failedOrders; }

    public long getOutOfStockOrders() { return outOfStockOrders; }
    public void setOutOfStockOrders(long outOfStockOrders) { this.outOfStockOrders = outOfStockOrders; }

    public long getProcessingOrders() { return processingOrders; }
    public void setProcessingOrders(long processingOrders) { this.processingOrders = processingOrders; }

    public long getRetryingOrders() { return retryingOrders; }
    public void setRetryingOrders(long retryingOrders) { this.retryingOrders = retryingOrders; }

    public long getDlqOrders() { return dlqOrders; }
    public void setDlqOrders(long dlqOrders) { this.dlqOrders = dlqOrders; }

    public int getActiveWorkers() { return activeWorkers; }
    public void setActiveWorkers(int activeWorkers) { this.activeWorkers = activeWorkers; }

    public int getQueueSize() { return queueSize; }
    public void setQueueSize(int queueSize) { this.queueSize = queueSize; }

    public double getOrdersPerSecond() { return ordersPerSecond; }
    public void setOrdersPerSecond(double ordersPerSecond) { this.ordersPerSecond = ordersPerSecond; }

    public double getAvgProcessingTimeMs() { return avgProcessingTimeMs; }
    public void setAvgProcessingTimeMs(double avgProcessingTimeMs) { this.avgProcessingTimeMs = avgProcessingTimeMs; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private long totalOrders;
        private long successfulOrders;
        private long failedOrders;
        private long outOfStockOrders;
        private long processingOrders;
        private long retryingOrders;
        private long dlqOrders;
        private int activeWorkers;
        private int queueSize;
        private double ordersPerSecond;
        private double avgProcessingTimeMs;

        public Builder totalOrders(long totalOrders) { this.totalOrders = totalOrders; return this; }
        public Builder successfulOrders(long successfulOrders) { this.successfulOrders = successfulOrders; return this; }
        public Builder failedOrders(long failedOrders) { this.failedOrders = failedOrders; return this; }
        public Builder outOfStockOrders(long outOfStockOrders) { this.outOfStockOrders = outOfStockOrders; return this; }
        public Builder processingOrders(long processingOrders) { this.processingOrders = processingOrders; return this; }
        public Builder retryingOrders(long retryingOrders) { this.retryingOrders = retryingOrders; return this; }
        public Builder dlqOrders(long dlqOrders) { this.dlqOrders = dlqOrders; return this; }
        public Builder activeWorkers(int activeWorkers) { this.activeWorkers = activeWorkers; return this; }
        public Builder queueSize(int queueSize) { this.queueSize = queueSize; return this; }
        public Builder ordersPerSecond(double ordersPerSecond) { this.ordersPerSecond = ordersPerSecond; return this; }
        public Builder avgProcessingTimeMs(double avgProcessingTimeMs) { this.avgProcessingTimeMs = avgProcessingTimeMs; return this; }

        public SystemMetricsDTO build() {
            return new SystemMetricsDTO(totalOrders, successfulOrders, failedOrders, outOfStockOrders, processingOrders, retryingOrders, dlqOrders, activeWorkers, queueSize, ordersPerSecond, avgProcessingTimeMs);
        }
    }
}
