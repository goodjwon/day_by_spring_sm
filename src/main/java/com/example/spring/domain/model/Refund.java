package com.example.spring.domain.model;

import com.example.spring.domain.vo.Money;
import com.example.spring.exception.RefundException;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 환불 정보 엔티티
 */
@Entity
@Table(name = "refunds")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Refund {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 주문 정보 (N:1 관계 - 부분 환불을 위해)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    // 환불 상태
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private RefundStatus status = RefundStatus.REQUESTED;

    // 환불 금액
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "amount", nullable = false)),
            @AttributeOverride(name = "currency", column = @Column(name = "amount_currency", length = 3))
    })
    private Money amount;

    // 환불 사유
    @Column(nullable = false, length = 1000)
    private String reason;

    // 환불 요청자 (관리자가 대신 요청할 수도 있음)
    private String requestedBy;

    // 날짜 정보
    @Column(nullable = false)
    private LocalDateTime requestDate;

    private LocalDateTime approvedDate;
    private LocalDateTime rejectedDate;
    private LocalDateTime completedDate;

    // 승인/거부 정보
    private String approvedBy;
    private String rejectedBy;
    private String rejectionReason;

    // 환불 계좌 정보
    private String bankName;
    private String accountNumber;
    private String accountHolder;

    // 환불 처리 정보
    private String refundTransactionId;  // 환불 거래 ID
    private String processingMemo;       // 처리 메모

    // 생성 및 수정 일시
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
        updatedDate = LocalDateTime.now();
        requestDate = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedDate = LocalDateTime.now();
    }

    // 비즈니스 로직 메서드
    public void approve(String approver) {
        if (this.status != RefundStatus.REQUESTED) {
            throw new RefundException.InvalidRefundStateException("요청된 환불만 승인할 수 있습니다.");
        }
        this.status = RefundStatus.APPROVED;
        this.approvedBy = approver;
        this.approvedDate = LocalDateTime.now();
    }

    public void reject(String rejecter, String reason) {
        if (this.status != RefundStatus.REQUESTED) {
            throw new RefundException.InvalidRefundStateException("요청된 환불만 거부할 수 있습니다.");
        }
        this.status = RefundStatus.REJECTED;
        this.rejectedBy = rejecter;
        this.rejectionReason = reason;
        this.rejectedDate = LocalDateTime.now();
    }

    public void startProcessing() {
        if (this.status != RefundStatus.APPROVED) {
            throw new RefundException.InvalidRefundStateException("승인된 환불만 처리를 시작할 수 있습니다.");
        }
        this.status = RefundStatus.PROCESSING;
    }

    public void complete(String transactionId) {
        if (this.status != RefundStatus.PROCESSING) {
            throw new RefundException.InvalidRefundStateException("처리중인 환불만 완료할 수 있습니다.");
        }
        this.status = RefundStatus.COMPLETED;
        this.refundTransactionId = transactionId;
        this.completedDate = LocalDateTime.now();
    }

    public void fail(String memo) {
        this.status = RefundStatus.FAILED;
        this.processingMemo = memo;
    }

    public boolean isCompleted() {
        return this.status == RefundStatus.COMPLETED;
    }

    public boolean canCancel() {
        return this.status == RefundStatus.REQUESTED || this.status == RefundStatus.APPROVED;
    }

    /**
     * 환불 계좌 정보 설정
     */
    public void updateBankAccount(String bankName, String accountNumber, String accountHolder) {
        this.bankName = bankName;
        this.accountNumber = accountNumber;
        this.accountHolder = accountHolder;
    }
}