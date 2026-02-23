package com.example.spring.domain.model;

import com.example.spring.domain.vo.Money;
import com.example.spring.exception.PaymentException;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 결제 정보 엔티티
 */
@Entity
@Table(name = "payments")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 주문 정보 (1:1 관계)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    // 결제 수단
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod method;

    // 결제 상태
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    // 결제 금액
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "amount", nullable = false)),
            @AttributeOverride(name = "currency", column = @Column(name = "amount_currency", length = 3))
    })
    private Money amount;

    // 결제 일시
    private LocalDateTime paymentDate;

    // 거래 ID (PG사에서 제공)
    @Column(unique = true)
    private String transactionId;

    // PG사 정보
    private String pgProvider;  // 예: 토스페이먼츠, 나이스페이, KG이니시스 등

    // 카드 정보 (선택적)
    private String cardCompany;
    private String cardNumber;  // 마스킹된 카드번호 (예: 1234-****-****-5678)
    private Integer installmentMonths;  // 할부 개월수

    // 실패 정보
    private String failureReason;
    private LocalDateTime failedDate;

    // 취소/환불 정보
    private LocalDateTime cancelledDate;
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "refunded_amount")),
            @AttributeOverride(name = "currency", column = @Column(name = "refunded_currency", length = 3))
    })
    private Money refundedAmount;
    private LocalDateTime refundedDate;

    // 생성 및 수정 일시
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
        updatedDate = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedDate = LocalDateTime.now();
    }

    // 비즈니스 로직 메서드
    public void complete(String transactionId) {
        if (this.status != PaymentStatus.PENDING) {
            throw new PaymentException.InvalidPaymentStateException("대기 중인 결제만 완료 처리할 수 있습니다.");
        }
        this.status = PaymentStatus.COMPLETED;
        this.transactionId = transactionId;
        this.paymentDate = LocalDateTime.now();
    }

    public void fail(String reason) {
        this.status = PaymentStatus.FAILED;
        this.failureReason = reason;
        this.failedDate = LocalDateTime.now();
    }

    public void cancel() {
        if (this.status != PaymentStatus.COMPLETED) {
            throw new PaymentException.InvalidPaymentStateException("완료된 결제만 취소할 수 있습니다.");
        }
        this.status = PaymentStatus.CANCELLED;
        this.cancelledDate = LocalDateTime.now();
    }

    public void refund(Money refundAmount) {
        if (this.status != PaymentStatus.COMPLETED && this.status != PaymentStatus.PARTIAL_REFUNDED) {
            throw new PaymentException.InvalidPaymentStateException("완료된 결제만 환불할 수 있습니다.");
        }

        if (refundAmount.getAmount().compareTo(this.amount.getAmount()) > 0) {
            throw new PaymentException.InvalidPaymentAmountException("환불 금액이 결제 금액을 초과할 수 없습니다.");
        }

        this.refundedAmount = refundAmount;
        this.refundedDate = LocalDateTime.now();

        if (refundAmount.getAmount().compareTo(this.amount.getAmount()) == 0) {
            this.status = PaymentStatus.REFUNDED;
        } else {
            this.status = PaymentStatus.PARTIAL_REFUNDED;
        }
    }

    public boolean isCompleted() {
        return this.status == PaymentStatus.COMPLETED;
    }

    public boolean isRefundable() {
        return this.status == PaymentStatus.COMPLETED ||
                this.status == PaymentStatus.PARTIAL_REFUNDED;
    }

    /**
     * 주문에 연결 (package-private)
     */
    void attachToOrder(Order order) {
        this.order = order;
    }

    /**
     * 카드 정보 설정
     */
    public void updateCardInfo(String cardCompany, String cardNumber, Integer installmentMonths) {
        this.cardCompany = cardCompany;
        this.cardNumber = cardNumber;
        this.installmentMonths = installmentMonths;
    }

    /**
     * PG사 설정
     */
    public void setPgProvider(String pgProvider) {
        this.pgProvider = pgProvider;
    }
}