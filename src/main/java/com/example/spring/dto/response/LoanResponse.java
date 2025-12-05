package com.example.spring.dto.response;

import com.example.spring.entity.Book;
import com.example.spring.entity.Loan;
import com.example.spring.entity.LoanStatus;
import com.example.spring.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanResponse {
    private Long id;
    private Member member;
    private Book book;
    private LoanStatus status;
    private BigDecimal overdueFee;
    private LocalDateTime dueDate;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    /**
     * Loan 엔티티를 LoanResponse로 변환
     */
    public static LoanResponse form(Loan loan) {
        return LoanResponse.builder()
                .id(loan.getId())
                .member(loan.getMember())
                .book(loan.getBook())
                .status(loan.getStatus())
                .overdueFee(loan.getOverdueFee())
                .dueDate(loan.getDueDate())
                .createdDate(loan.getCreatedDate())
                .updatedDate(loan.getUpdatedDate())
                .build();
    }
}
