package com.example.patten.observer.after;

public class OrderPlacedEvent {
    private final String productId;
    private final String address;

    public OrderPlacedEvent(String productId, String orderId) {
        this.productId = productId;
        this.address = orderId;
    }

    public String getProductId() {
        return productId;
    }
    public String getAddress() {
        return address;
    }
}
