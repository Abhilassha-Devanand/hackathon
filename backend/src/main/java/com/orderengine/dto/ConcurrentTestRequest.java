package com.orderengine.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class ConcurrentTestRequest {

    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotNull(message = "Number of orders is required")
    @Min(value = 1, message = "Number of orders must be at least 1")
    private Integer numberOfOrders;

    @NotNull(message = "Quantity per order is required")
    @Min(value = 1, message = "Quantity per order must be at least 1")
    private Integer quantityPerOrder;

    private Integer threadPoolSize;
    private Double failureSimulationPercentage;
    private Integer initialStockOverride;

    public ConcurrentTestRequest() {}

    public ConcurrentTestRequest(Long productId, Integer numberOfOrders, Integer quantityPerOrder, Integer threadPoolSize, Double failureSimulationPercentage, Integer initialStockOverride) {
        this.productId = productId;
        this.numberOfOrders = numberOfOrders;
        this.quantityPerOrder = quantityPerOrder;
        this.threadPoolSize = threadPoolSize;
        this.failureSimulationPercentage = failureSimulationPercentage;
        this.initialStockOverride = initialStockOverride;
    }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public Integer getNumberOfOrders() { return numberOfOrders; }
    public void setNumberOfOrders(Integer numberOfOrders) { this.numberOfOrders = numberOfOrders; }

    public Integer getQuantityPerOrder() { return quantityPerOrder; }
    public void setQuantityPerOrder(Integer quantityPerOrder) { this.quantityPerOrder = quantityPerOrder; }

    public Integer getThreadPoolSize() { return threadPoolSize; }
    public void setThreadPoolSize(Integer threadPoolSize) { this.threadPoolSize = threadPoolSize; }

    public Double getFailureSimulationPercentage() { return failureSimulationPercentage; }
    public void setFailureSimulationPercentage(Double failureSimulationPercentage) { this.failureSimulationPercentage = failureSimulationPercentage; }

    public Integer getInitialStockOverride() { return initialStockOverride; }
    public void setInitialStockOverride(Integer initialStockOverride) { this.initialStockOverride = initialStockOverride; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long productId;
        private Integer numberOfOrders;
        private Integer quantityPerOrder;
        private Integer threadPoolSize;
        private Double failureSimulationPercentage;
        private Integer initialStockOverride;

        public Builder productId(Long productId) { this.productId = productId; return this; }
        public Builder numberOfOrders(Integer numberOfOrders) { this.numberOfOrders = numberOfOrders; return this; }
        public Builder quantityPerOrder(Integer quantityPerOrder) { this.quantityPerOrder = quantityPerOrder; return this; }
        public Builder threadPoolSize(Integer threadPoolSize) { this.threadPoolSize = threadPoolSize; return this; }
        public Builder failureSimulationPercentage(Double failureSimulationPercentage) { this.failureSimulationPercentage = failureSimulationPercentage; return this; }
        public Builder initialStockOverride(Integer initialStockOverride) { this.initialStockOverride = initialStockOverride; return this; }

        public ConcurrentTestRequest build() {
            return new ConcurrentTestRequest(productId, numberOfOrders, quantityPerOrder, threadPoolSize, failureSimulationPercentage, initialStockOverride);
        }
    }
}
