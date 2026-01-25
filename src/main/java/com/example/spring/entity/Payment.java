package com.example.spring.entity;

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
    private int amount;

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
    // onUpdate
    // complete
    // fail
    // cancel
    // refund
    // isCompleted
}
