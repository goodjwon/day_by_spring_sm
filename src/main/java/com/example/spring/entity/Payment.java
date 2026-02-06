package com.example.spring.entity;

import com.example.spring.exception.OrderException;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod method;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(nullable = false)
    private BigDecimal amount;

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
    private BigDecimal refundedAmount;
    private LocalDateTime refundedDate;

    // 생성 및 수정 일시
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    // todo. 1월. 24일 과제.
    // onCreate
    @PrePersist
    protected void onCreate(){
        createdDate = LocalDateTime.now();
        updatedDate = LocalDateTime.now();
    }
    // onUpdate
    @PreUpdate
    protected void onUpdate(){
        updatedDate = LocalDateTime.now();
    }
    // complete
    public void complete(){
        if (status != PaymentStatus.PENDING) {
            throw new OrderException.InvalidOrderStateException("확인할 수 없는 결재 수단입니다" + this.status);
        }
        this.status = PaymentStatus.COMPLETED;
        this.paymentDate = LocalDateTime.now();
    }
    // fail
    public void fail(String reason){
        this.status = PaymentStatus.FAILED;
        this.failureReason = reason;
        this.failedDate = LocalDateTime.now();
    }
    // cancel
    public void cancel(){
        this.status = PaymentStatus.CANCELLED;
        this.cancelledDate = LocalDateTime.now();
    }
    // refund
    public void refund(BigDecimal amount){
        this.refundedAmount = amount;
        this.refundedDate = LocalDateTime.now();
    }
    //partialRefund
    public void partialRefund(BigDecimal amount){
        this.refundedAmount = amount;
        this.refundedDate = LocalDateTime.now();
    }
    // isCompleted
    public boolean isCompleted(){
        return this.status == PaymentStatus.COMPLETED;
    }
}
