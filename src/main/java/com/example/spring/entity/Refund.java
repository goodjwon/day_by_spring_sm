package com.example.spring.entity;


import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "refund")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Refund {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private RefundStatus status = RefundStatus.REQUESTED;

    @Column(nullable = false)
    private BigDecimal amount;

    private String reason;
    private String requestedBy;
    private LocalDateTime requestDate;

    private LocalDateTime approvedDate;
    private LocalDateTime rejectedDate;
    private LocalDateTime completedDate;
    private LocalDateTime refundDate;

    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    private String refundTransactionId;

    @PrePersist
    protected void onCreate(){
        createdDate = LocalDateTime.now();
        updatedDate = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate(){
        updatedDate = LocalDateTime.now();
    }

    public void requestRefund(BigDecimal amount, String requestedBy){
        this.amount = amount;
        this.requestedBy = requestedBy;
        this.requestDate = LocalDateTime.now();
    }

    public void complete(){
        this.status = RefundStatus.COMPLETED;
        this.completedDate = LocalDateTime.now();
    }

    public void reject(String reason){
        this.status = RefundStatus.REJECTED;
        this.rejectedDate = LocalDateTime.now();
        this.reason = reason;
    }

    public void approve(){
        this.status = RefundStatus.APPROVED;
        this.approvedDate = LocalDateTime.now();
    }

    public boolean isCompleted(){
        return this.status == RefundStatus.COMPLETED;
    }

    public boolean isRejected(){
        return this.status == RefundStatus.REJECTED;
    }

    public boolean isApproved(){
        return this.status == RefundStatus.APPROVED;
    }

    public boolean isPending() {
        return this.status == RefundStatus.REQUESTED;
    }
}
