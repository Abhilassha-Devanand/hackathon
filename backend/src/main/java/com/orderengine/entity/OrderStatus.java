package com.orderengine.entity;

public enum OrderStatus {
    PENDING,
    PROCESSING,
    RESERVED,
    CONFIRMED,
    FAILED,
    RETRYING,
    OUT_OF_STOCK,
    CANCELLED,
    DLQ
}
