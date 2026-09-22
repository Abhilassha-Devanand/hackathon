package com.orderengine.dto;

import com.orderengine.entity.DeadLetterQueue;
import java.time.LocalDateTime;

public class DlqResponse {
    private Long id;
    private Long orderId;
    private String failureReason;
    private Integer retryCount;
    private String payload;
    private String lastError;
    private LocalDateTime createdAt;

    public DlqResponse() {}

    public DlqResponse(Long id, Long orderId, String failureReason, Integer retryCount, String payload, String lastError, LocalDateTime createdAt) {
        this.id = id;
        this.orderId = orderId;
        this.failureReason = failureReason;
        this.retryCount = retryCount;
        this.payload = payload;
        this.lastError = lastError;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }

    public Integer getRetryCount() { return retryCount; }
    public void setRetryCount(Integer retryCount) { this.retryCount = retryCount; }

    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }

    public String getLastError() { return lastError; }
    public void setLastError(String lastError) { this.lastError = lastError; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static DlqResponse fromEntity(DeadLetterQueue dlq) {
        return new DlqResponse(
                dlq.getId(),
                dlq.getOrderId(),
                dlq.getFailureReason(),
                dlq.getRetryCount(),
                dlq.getPayload(),
                dlq.getLastError(),
                dlq.getCreatedAt()
        );
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private Long orderId;
        private String failureReason;
        private Integer retryCount;
        private String payload;
        private String lastError;
        private LocalDateTime createdAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder orderId(Long orderId) { this.orderId = orderId; return this; }
        public Builder failureReason(String failureReason) { this.failureReason = failureReason; return this; }
        public Builder retryCount(Integer retryCount) { this.retryCount = retryCount; return this; }
        public Builder payload(String payload) { this.payload = payload; return this; }
        public Builder lastError(String lastError) { this.lastError = lastError; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public DlqResponse build() {
            return new DlqResponse(id, orderId, failureReason, retryCount, payload, lastError, createdAt);
        }
    }
}
