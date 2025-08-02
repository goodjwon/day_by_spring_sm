package com.example.spring.entity;

public enum MembershipType {
    REGULAR(5, true),
    PREMIUM(10, true),
    SUSPENDED(0, false);

    private final int maxBorrowCount;
    private final boolean active;

    MembershipType(int maxBorrowCount, boolean active) {
        this.maxBorrowCount = maxBorrowCount;
        this.active = active;
    }

    public int getMaxBorrowCount() { return maxBorrowCount; }
    public boolean isActive() { return active; }
}
