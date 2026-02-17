package com.example.spring.entity;


import com.example.spring.domain.vo.Money;
import com.example.spring.exception.RefundException;
import jakarta.persistence.*;
import lombok.*;
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
    private Money amount;

    private String bankName;
    private String accountNumber;
    private String accountHolder;

    private String reason;
    private String requestedBy;
    private LocalDateTime requestDate;
    private String approvedBy;
    private LocalDateTime approvedDate;
    private String rejectedBy;
    private String rejectionReason;
    private LocalDateTime rejectedDate;
    private LocalDateTime completedDate;
    private LocalDateTime refundDate;
    private String processingMemo;

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

    public void requestRefund(Money amount, String requestedBy){
        this.amount = amount;
        this.requestedBy = requestedBy;
        this.requestDate = LocalDateTime.now();
    }

    public void complete(String transactionId){
        if (!RefundStatus.PROCESSING.equals(status)) {
            throw new RefundException.InvalidRefundStateException("처리중인 환불만 완료");
        }
        this.status = RefundStatus.COMPLETED;
        this.refundTransactionId = transactionId;
        this.completedDate = LocalDateTime.now();
    }

    public void reject(String rejecter, String rejectionReason) {
        if (!RefundStatus.REQUESTED.equals(status)) {
            throw new RefundException.InvalidRefundStateException("요청된 환불만 거부");
        }
        this.status = RefundStatus.REJECTED;
        this.rejectedBy = rejecter;
        this.rejectionReason = rejectionReason;
        this.rejectedDate = LocalDateTime.now();
    }

    public void approve(String approver){
        if (!RefundStatus.REQUESTED.equals(status)) {
            throw new RefundException.InvalidRefundStateException("요청된 환불만 승인");
        }
        this.status = RefundStatus.APPROVED;
        this.approvedBy = approver;
        this.approvedDate = LocalDateTime.now();
    }

    public void fail(String memo) {
        this.status = RefundStatus.FAILED;
        this.processingMemo = memo;
    }

    public void startProcessing() {
        if (!RefundStatus.APPROVED.equals(status)) {
            throw new RefundException.InvalidRefundStateException("승인된 환불만 처리를 시작");
        }
        this.status = RefundStatus.PROCESSING;
        this.requestDate = LocalDateTime.now();
    }

    public boolean canCancel() {
        return this.status == RefundStatus.REQUESTED || this.status == RefundStatus.APPROVED;
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
