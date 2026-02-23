package com.example.spring.domain.model;

import com.example.spring.domain.vo.Money;
import com.example.spring.exception.ErrorMessages;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
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
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"member", "book", "createdDate", "updatedDate"})
public class Loan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "{validation.loan.member.required}")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @NotNull(message = "{validation.loan.book.required}")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @NotNull(message = "{validation.loan.loanDate.required}")
    @Column(name = "loan_date", nullable = false)
    private LocalDateTime loanDate;

    @NotNull(message = "{validation.loan.dueDate.required}")
    @Column(name = "due_date", nullable = false)
    private LocalDateTime dueDate;

    @Column(name = "return_date")
    private LocalDateTime returnDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private LoanStatus status = LoanStatus.ACTIVE;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "overdue_fee", precision = 10, scale = 2)),
            @AttributeOverride(name = "currency", column = @Column(name = "overdue_fee_currency", length = 3))
    })
    @Builder.Default
    private Money overdueFee = Money.zero();

    @Column(name = "extension_count")
    @Builder.Default
    private Integer extensionCount = 0;

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
            return Money.zero();
        }
        return Money.of(overdueDays * 1000);
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
     * 대여 연장 (지정 일수)
     * @param days 연장 일수
     * @param maxExtensions 최대 연장 가능 횟수
     */
    public void extendLoan(int days, int maxExtensions) {
        if (this.returnDate != null) {
            throw new IllegalStateException(ErrorMessages.LOAN_RETURNED_CANNOT_EXTEND);
        }
        if (isOverdue()) {
            throw new IllegalStateException(ErrorMessages.LOAN_OVERDUE_CANNOT_EXTEND);
        }
        if (this.extensionCount >= maxExtensions) {
            throw new IllegalStateException(
                    String.format("연장 가능 횟수를 초과했습니다. (현재: %d, 최대: %d)", this.extensionCount, maxExtensions));
        }
        if (!canExtendNow()) {
            long daysUntilDue = getDaysUntilDue();
            throw new IllegalStateException(
                    String.format("반납 예정일 3일 전부터 연장 가능합니다. (반납 예정일까지 %d일 남음)", daysUntilDue));
        }
        this.dueDate = this.dueDate.plusDays(days);
        this.extensionCount++;
    }

    /**
     * 대여 연장 (기본 - 하위 호환성 유지)
     * @param days 연장 일수
     */
    public void extendLoan(int days) {
        extendLoan(days, Integer.MAX_VALUE);
    }

    /**
     * 반납 예정일까지 남은 일수
     */
    public long getDaysUntilDue() {
        if (this.returnDate != null || isOverdue()) {
            return 0;
        }
        return ChronoUnit.DAYS.between(LocalDateTime.now(), this.dueDate);
    }

    /**
     * 연장 가능 시기인지 확인 (반납 예정일 3일 전부터 가능)
     */
    public boolean canExtendNow() {
        if (this.returnDate != null || isOverdue()) {
            return false;
        }
        return getDaysUntilDue() <= 3;
    }

    /**
     * 대여 취소
     */
    public void cancel() {
        if (this.returnDate != null) {
            throw new IllegalStateException(ErrorMessages.LOAN_RETURNED_CANNOT_CANCEL);
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

    /**
     * BigDecimal로 연체료 금액 반환 (하위 호환성)
     */
    public BigDecimal getOverdueFeeAmount() {
        return overdueFee != null ? overdueFee.getAmount() : BigDecimal.ZERO;
    }

    /**
     * 관리자용 상태 변경
     */
    public void changeStatus(LoanStatus newStatus) {
        this.status = newStatus;
    }

    /**
     * 관리자용 반납 예정일 연장
     */
    public void adminExtendDueDate(LocalDateTime newDueDate) {
        this.extensionCount++;
        this.dueDate = newDueDate;
        this.status = LoanStatus.ACTIVE;
    }
}