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
    private Long memberId;
    private String memberName;
    private String memberEmail;
    private Long bookId;
    private String bookTitle;
    private String bookAuthor;
    private String bookIsbn;
    private LocalDateTime loanDate;
    private LocalDateTime dueDate;
    private LocalDateTime returnDate;
    private LoanStatus status;
    private BigDecimal overdueFee;
    private Boolean isOverdue;
    private Long overdueDays;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    /**
     * Loan 엔티티를 LoanResponse로 변환
     */
    public static LoanResponse form(Loan loan) {
        return LoanResponse.builder()
                .id(loan.getId())
                .memberId(loan.getMember().getId())
                .memberName(loan.getMember().getName())
                .memberEmail(loan.getMember().getEmail())
                .bookId(loan.getBook().getId())
                .bookTitle(loan.getBook().getTitle())
                .bookAuthor(loan.getBook().getAuthor())
                .bookIsbn(loan.getBook().getIsbn())
                .loanDate(loan.getLoanDate())
                .dueDate(loan.getDueDate())
                .returnDate(loan.getReturnDate())
                .status(loan.getStatus())
                .overdueFee(loan.getOverdueFee())
                .isOverdue(loan.isOverdue())
                .overdueDays(loan.getOverdueDays())
                .createdDate(loan.getCreatedDate())
                .updatedDate(loan.getUpdatedDate())
                .build();
    }
}
