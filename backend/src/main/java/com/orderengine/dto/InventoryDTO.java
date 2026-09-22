package com.orderengine.dto;

import com.orderengine.entity.ContentionLevel;
import java.time.LocalDateTime;

public class InventoryDTO {
    private Long productId;
    private String productName;
    private String sku;
    private Integer availableQuantity;
    private Integer reservedQuantity;
    private ContentionLevel contentionLevel;
    private Double requestsPerSecond;
    private Long successfulAllocations;
    private Long failedAllocations;
    private LocalDateTime updatedAt;

    public InventoryDTO() {}

    public InventoryDTO(Long productId, String productName, String sku, Integer availableQuantity, Integer reservedQuantity,
                        ContentionLevel contentionLevel, Double requestsPerSecond, Long successfulAllocations,
                        Long failedAllocations, LocalDateTime updatedAt) {
        this.productId = productId;
        this.productName = productName;
        this.sku = sku;
        this.availableQuantity = availableQuantity;
        this.reservedQuantity = reservedQuantity;
        this.contentionLevel = contentionLevel;
        this.requestsPerSecond = requestsPerSecond;
        this.successfulAllocations = successfulAllocations;
        this.failedAllocations = failedAllocations;
        this.updatedAt = updatedAt;
    }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public Integer getAvailableQuantity() { return availableQuantity; }
    public void setAvailableQuantity(Integer availableQuantity) { this.availableQuantity = availableQuantity; }

    public Integer getReservedQuantity() { return reservedQuantity; }
    public void setReservedQuantity(Integer reservedQuantity) { this.reservedQuantity = reservedQuantity; }

    public ContentionLevel getContentionLevel() { return contentionLevel; }
    public void setContentionLevel(ContentionLevel contentionLevel) { this.contentionLevel = contentionLevel; }

    public Double getRequestsPerSecond() { return requestsPerSecond; }
    public void setRequestsPerSecond(Double requestsPerSecond) { this.requestsPerSecond = requestsPerSecond; }

    public Long getSuccessfulAllocations() { return successfulAllocations; }
    public void setSuccessfulAllocations(Long successfulAllocations) { this.successfulAllocations = successfulAllocations; }

    public Long getFailedAllocations() { return failedAllocations; }
    public void setFailedAllocations(Long failedAllocations) { this.failedAllocations = failedAllocations; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long productId;
        private String productName;
        private String sku;
        private Integer availableQuantity;
        private Integer reservedQuantity;
        private ContentionLevel contentionLevel;
        private Double requestsPerSecond;
        private Long successfulAllocations;
        private Long failedAllocations;
        private LocalDateTime updatedAt;

        public Builder productId(Long productId) { this.productId = productId; return this; }
        public Builder productName(String productName) { this.productName = productName; return this; }
        public Builder sku(String sku) { this.sku = sku; return this; }
        public Builder availableQuantity(Integer availableQuantity) { this.availableQuantity = availableQuantity; return this; }
        public Builder reservedQuantity(Integer reservedQuantity) { this.reservedQuantity = reservedQuantity; return this; }
        public Builder contentionLevel(ContentionLevel contentionLevel) { this.contentionLevel = contentionLevel; return this; }
        public Builder requestsPerSecond(Double requestsPerSecond) { this.requestsPerSecond = requestsPerSecond; return this; }
        public Builder successfulAllocations(Long successfulAllocations) { this.successfulAllocations = successfulAllocations; return this; }
        public Builder failedAllocations(Long failedAllocations) { this.failedAllocations = failedAllocations; return this; }
        public Builder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public InventoryDTO build() {
            return new InventoryDTO(productId, productName, sku, availableQuantity, reservedQuantity, contentionLevel, requestsPerSecond, successfulAllocations, failedAllocations, updatedAt);
        }
    }
}
