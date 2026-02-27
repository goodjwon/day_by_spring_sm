package com.example.spring.domain.event;

import com.example.spring.domain.vo.Money;
import com.example.spring.domain.model.Loan;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 대출 연체 이벤트
 */
@Getter
public class LoanOverdueEvent {

    private final Loan loan;
    private final long overdueDays;
    private final Money currentOverdueFee;
    private final LocalDateTime occurredAt;

    public LoanOverdueEvent(Loan loan) {
        this.loan = loan;
        this.overdueDays = loan.getOverdueDays();
        this.currentOverdueFee = loan.calculateOverdueFee();
        this.occurredAt = LocalDateTime.now();
    }

    public Long getLoanId() {
        return loan.getId();
    }

    public Long getMemberId() {
        return loan.getMember() != null ? loan.getMember().getId() : null;
    }

    public String getMemberEmail() {
        return loan.getMember() != null ? loan.getMember().getEmail() : null;
    }

    public Long getBookId() {
        return loan.getBook() != null ? loan.getBook().getId() : null;
    }

    public String getBookTitle() {
        return loan.getBook() != null ? loan.getBook().getTitle() : null;
    }

    public LocalDateTime getDueDate() {
        return loan.getDueDate();
    }
}