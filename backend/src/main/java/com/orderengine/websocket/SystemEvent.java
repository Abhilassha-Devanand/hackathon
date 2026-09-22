package com.orderengine.websocket;

import java.time.LocalDateTime;

public class SystemEvent {
    private String eventType;
    private Long orderId;
    private Long productId;
    private Object data;
    private String timestamp;

    public SystemEvent() {}

    public SystemEvent(String eventType, Long orderId, Long productId, Object data, String timestamp) {
        this.eventType = eventType;
        this.orderId = orderId;
        this.productId = productId;
        this.data = data;
        this.timestamp = timestamp;
    }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public Object getData() { return data; }
    public void setData(Object data) { this.data = data; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public static SystemEvent create(String eventType, Long orderId, Long productId, Object data) {
        return new SystemEvent(eventType, orderId, productId, data, LocalDateTime.now().toString());
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String eventType;
        private Long orderId;
        private Long productId;
        private Object data;
        private String timestamp;

        public Builder eventType(String eventType) { this.eventType = eventType; return this; }
        public Builder orderId(Long orderId) { this.orderId = orderId; return this; }
        public Builder productId(Long productId) { this.productId = productId; return this; }
        public Builder data(Object data) { this.data = data; return this; }
        public Builder timestamp(String timestamp) { this.timestamp = timestamp; return this; }

        public SystemEvent build() {
            return new SystemEvent(eventType, orderId, productId, data, timestamp);
        }
    }
}
