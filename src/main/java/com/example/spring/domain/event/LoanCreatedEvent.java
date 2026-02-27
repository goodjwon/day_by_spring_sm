package com.example.spring.domain.event;

import com.example.spring.domain.model.Loan;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 대출 생성 이벤트
 */
@Getter
public class LoanCreatedEvent {

    private final Loan loan;
    private final LocalDateTime occurredAt;

    public LoanCreatedEvent(Loan loan) {
        this.loan = loan;
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

    public LocalDateTime getDueDate() {
        return loan.getDueDate();
    }
}