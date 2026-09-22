package com.orderengine.dto;

public class ConcurrentTestResponse {
    private String testName;
    private Long productId;
    private int initialStock;
    private int finalStock;
    private int totalAttempted;
    private int successful;
    private int outOfStock;
    private int failed;
    private int sentToDlq;
    private long executionDurationMs;
    private double peakOrdersPerSecond;
    private boolean inventorySafetyVerified;
    private String message;

    public ConcurrentTestResponse() {}

    public ConcurrentTestResponse(String testName, Long productId, int initialStock, int finalStock, int totalAttempted, int successful, int outOfStock, int failed, int sentToDlq, long executionDurationMs, double peakOrdersPerSecond, boolean inventorySafetyVerified, String message) {
        this.testName = testName;
        this.productId = productId;
        this.initialStock = initialStock;
        this.finalStock = finalStock;
        this.totalAttempted = totalAttempted;
        this.successful = successful;
        this.outOfStock = outOfStock;
        this.failed = failed;
        this.sentToDlq = sentToDlq;
        this.executionDurationMs = executionDurationMs;
        this.peakOrdersPerSecond = peakOrdersPerSecond;
        this.inventorySafetyVerified = inventorySafetyVerified;
        this.message = message;
    }

    public String getTestName() { return testName; }
    public void setTestName(String testName) { this.testName = testName; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public int getInitialStock() { return initialStock; }
    public void setInitialStock(int initialStock) { this.initialStock = initialStock; }

    public int getFinalStock() { return finalStock; }
    public void setFinalStock(int finalStock) { this.finalStock = finalStock; }

    public int getTotalAttempted() { return totalAttempted; }
    public void setTotalAttempted(int totalAttempted) { this.totalAttempted = totalAttempted; }

    public int getSuccessful() { return successful; }
    public void setSuccessful(int successful) { this.successful = successful; }

    public int getOutOfStock() { return outOfStock; }
    public void setOutOfStock(int outOfStock) { this.outOfStock = outOfStock; }

    public int getFailed() { return failed; }
    public void setFailed(int failed) { this.failed = failed; }

    public int getSentToDlq() { return sentToDlq; }
    public void setSentToDlq(int sentToDlq) { this.sentToDlq = sentToDlq; }

    public long getExecutionDurationMs() { return executionDurationMs; }
    public void setExecutionDurationMs(long executionDurationMs) { this.executionDurationMs = executionDurationMs; }

    public double getPeakOrdersPerSecond() { return peakOrdersPerSecond; }
    public void setPeakOrdersPerSecond(double peakOrdersPerSecond) { this.peakOrdersPerSecond = peakOrdersPerSecond; }

    public boolean isInventorySafetyVerified() { return inventorySafetyVerified; }
    public void setInventorySafetyVerified(boolean inventorySafetyVerified) { this.inventorySafetyVerified = inventorySafetyVerified; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String testName;
        private Long productId;
        private int initialStock;
        private int finalStock;
        private int totalAttempted;
        private int successful;
        private int outOfStock;
        private int failed;
        private int sentToDlq;
        private long executionDurationMs;
        private double peakOrdersPerSecond;
        private boolean inventorySafetyVerified;
        private String message;

        public Builder testName(String testName) { this.testName = testName; return this; }
        public Builder productId(Long productId) { this.productId = productId; return this; }
        public Builder initialStock(int initialStock) { this.initialStock = initialStock; return this; }
        public Builder finalStock(int finalStock) { this.finalStock = finalStock; return this; }
        public Builder totalAttempted(int totalAttempted) { this.totalAttempted = totalAttempted; return this; }
        public Builder successful(int successful) { this.successful = successful; return this; }
        public Builder outOfStock(int outOfStock) { this.outOfStock = outOfStock; return this; }
        public Builder failed(int failed) { this.failed = failed; return this; }
        public Builder sentToDlq(int sentToDlq) { this.sentToDlq = sentToDlq; return this; }
        public Builder executionDurationMs(long executionDurationMs) { this.executionDurationMs = executionDurationMs; return this; }
        public Builder peakOrdersPerSecond(double peakOrdersPerSecond) { this.peakOrdersPerSecond = peakOrdersPerSecond; return this; }
        public Builder inventorySafetyVerified(boolean inventorySafetyVerified) { this.inventorySafetyVerified = inventorySafetyVerified; return this; }
        public Builder message(String message) { this.message = message; return this; }

        public ConcurrentTestResponse build() {
            return new ConcurrentTestResponse(testName, productId, initialStock, finalStock, totalAttempted, successful, outOfStock, failed, sentToDlq, executionDurationMs, peakOrdersPerSecond, inventorySafetyVerified, message);
        }
    }
}
