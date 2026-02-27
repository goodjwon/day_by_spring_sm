package com.example.spring.domain.event;

import com.example.spring.domain.vo.Money;
import com.example.spring.domain.model.Loan;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 도서 반납 이벤트
 */
@Getter
public class LoanReturnedEvent {

    private final Loan loan;
    private final boolean wasOverdue;
    private final Money overdueFee;
    private final LocalDateTime occurredAt;

    public LoanReturnedEvent(Loan loan) {
        this(loan, loan.getOverdueFee() != null && !loan.getOverdueFee().isZero());
    }

    public LoanReturnedEvent(Loan loan, boolean wasOverdue) {
        this.loan = loan;
        this.wasOverdue = wasOverdue;
        this.overdueFee = loan.getOverdueFee();
        this.occurredAt = LocalDateTime.now();
    }

    public Long getLoanId() {
        return loan.getId();
    }

    public Long getMemberId() {
        return loan.getMember() != null ? loan.getMember().getId() : null;
    }

    public Long getBookId() {
        return loan.getBook() != null ? loan.getBook().getId() : null;
    }

    public String getBookTitle() {
        return loan.getBook() != null ? loan.getBook().getTitle() : null;
    }
}