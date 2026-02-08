package com.example.spring.entity;

import com.example.spring.domain.vo.Money;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "loan", indexes = {
        @Index(name = "idx_loan_member_id", columnList = "member_id"),
        @Index(name = "idx_loan_book_id", columnList = "book_id"),
        @Index(name = "idx_loan_date", columnList = "loan_date"),
        @Index(name = "idx_loan_status", columnList = "status")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Loan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "회원 정보는 필수입니다")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @NotNull(message = "도서 정보는 필수입니다")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @NotNull(message = "대여일자는 필수입니다")
    @Column(name = "loan_date", nullable = false)
    private LocalDateTime loanDate;

    @NotNull(message = "반납예정일은 필수입니다")
    @Column(name = "due_date", nullable = false)
    private LocalDateTime dueDate;

    @Column(name = "return_date")
    private LocalDateTime returnDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private LoanStatus status = LoanStatus.ACTIVE;

    @Column(name = "overdue_fee", precision = 10, scale = 2)
    @Builder.Default
    private Money overdueFee = Money.ZERO;

    private Integer extensionCount;

    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    /**
     * 연체 여부 확인
     */
    public boolean isOverdue() {
        if (returnDate != null) {
            return false; // 이미 반납된 경우
        }
        return LocalDateTime.now().isAfter(dueDate);
    }

    /**
     * 연체 일수 계산
     */
    public long getOverdueDays() {
        if (!isOverdue()) {
            return 0;
        }
        return ChronoUnit.DAYS.between(dueDate, LocalDateTime.now());
    }

    /**
     * 연체료 계산 (일당 1000원)
     */
    public Money calculateOverdueFee() {
        long overdueDays = getOverdueDays();
        if (overdueDays <= 0) {
            return Money.ZERO;
        }
        return Money.of(BigDecimal.valueOf(overdueDays * 1000));
    }

    /**
     * 도서 반납 처리
     */
    public void returnBook() {
        // returnDate 설정 전에 연체료를 먼저 계산해야 함
        this.overdueFee = calculateOverdueFee();
        this.returnDate = LocalDateTime.now();
        this.status = LoanStatus.RETURNED;
    }

    /**
     * 대여 연장 (14일)
     */
    public void extendLoan(int days) {
        if (this.returnDate != null) {
            throw new IllegalStateException("이미 반납된 대여는 연장할 수 없습니다");
        }
        if (isOverdue()) {
            throw new IllegalStateException("연체된 대여는 연장할 수 없습니다");
        }
        this.dueDate = this.dueDate.plusDays(days);
    }

    /**
     * 대여 취소
     */
    public void cancel() {
        if (this.returnDate != null) {
            throw new IllegalStateException("이미 반납된 대여는 취소할 수 없습니다");
        }
        this.status = LoanStatus.CANCELLED;
    }

    /**
     * 대여 상태 업데이트 (연체 확인)
     */
    public void updateStatus() {
        if (this.returnDate != null) {
            this.status = LoanStatus.RETURNED;
        } else if (isOverdue()) {
            this.status = LoanStatus.OVERDUE;
            this.overdueFee = calculateOverdueFee();
        } else {
            this.status = LoanStatus.ACTIVE;
        }
    }
}