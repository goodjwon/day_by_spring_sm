package com.example.spring.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "loan")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Loan {
    //id, member_id, book_id, loan_date, due_date, return_date

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

    @Column(name = "status", nullable = false, length = 20)
    private LoanStatus status;

    @Column(name = "overdue_fee", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal overdueFee = BigDecimal.ZERO;

    @NotNull(message = "대여일자는 필수입니다")
    @Column(name = "loan_date", nullable = false)
    private LocalDateTime loanDate;

    @Column(name = "due_date", nullable = false)
    private LocalDateTime dueDate;


    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @Column(name = "return_date")
    private LocalDateTime returnDate;


    /**
     * 연체 여부 확인
     * 연체는 납부하기로한 시간이 지난 경우. 지금시점.
     */
    public boolean isOverdue(){
        return isOverdue(LocalDateTime.now());
    }

    public boolean isOverdue(LocalDateTime currentTime){
        if(returnDate != null){
            return false;   // 이미 반납된 경우.
        }

        return currentTime.isAfter(this.dueDate);
    }


    /**
     * 연체 일수 계산
     */
    public long getOverdueDays(LocalDateTime currentTime){
        if (!isOverdue(currentTime)) {
            return 0;
        }

        return ChronoUnit.DAYS.between(dueDate, currentTime);
    }

    public long getOverdueDays(){
        return getOverdueDays(LocalDateTime.now());
    }

    /**
     * 연체료 계산 (일당 1000원)
     */
    public static final BigDecimal FEE_PAR_DAY = new BigDecimal("1000");
    public BigDecimal calculateOverdueFee(LocalDateTime currentTime){
        long days = getOverdueDays(currentTime);
        return  FEE_PAR_DAY.multiply(BigDecimal.valueOf(days));
    }

    /**
     * 도서 반납 처리
     */
    public void returnBook(LocalDateTime returnTime) {
        if (this.returnDate != null) {
            throw new IllegalStateException("반납된 도서입니다");
        }

        this.returnDate = returnTime;
        this.status = LoanStatus.RETURNED;

        this.overdueFee = calculateOverdueFee(returnTime);
    }

    public void returnBook() {
        returnBook(LocalDateTime.now());
    }

    /**
     * 대여 연장 (14일)
     */


    /**
     * 대여 취소
     */

    /**
     * 대여 상태 업데이트 (연체 확인)
     */

    /**
     * 대여 상태 업데이트 (연체 확인)
     */
    public void updateStatus() {
        if (this.returnDate != null) {
            this.status = LoanStatus.RETURNED;
        } else {
            this.status = LoanStatus.ACTIVE;
        }
    }



}
