package com.example.spring.application.dto.response;

import com.example.spring.domain.model.Loan;
import com.example.spring.domain.model.LoanStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 대여 응답 DTO
 */
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
    private Integer extensionCount;
    private Boolean isOverdue;
    private Long overdueDays;
    private Long daysUntilDue;
    private Boolean canExtendNow;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    /**
     * Loan 엔티티를 LoanResponse로 변환
     */
    public static LoanResponse from(Loan loan) {
        return LoanResponse.builder()
                .id(loan.getId())
                .memberId(loan.getMember().getId())
                .memberName(loan.getMember().getName())
                .memberEmail(loan.getMember().getEmail())
                .bookId(loan.getBook().getId())
                .bookTitle(loan.getBook().getTitle())
                .bookAuthor(loan.getBook().getAuthor())
                .bookIsbn(loan.getBook().getIsbn() != null ? loan.getBook().getIsbn().getValue() : null)
                .loanDate(loan.getLoanDate())
                .dueDate(loan.getDueDate())
                .returnDate(loan.getReturnDate())
                .status(loan.getStatus())
                .overdueFee(loan.getOverdueFee() != null ? loan.getOverdueFee().getAmount() : BigDecimal.ZERO)
                .extensionCount(loan.getExtensionCount())
                .isOverdue(loan.isOverdue())
                .overdueDays(loan.getOverdueDays())
                .daysUntilDue(loan.getDaysUntilDue())
                .canExtendNow(loan.canExtendNow())
                .createdDate(loan.getCreatedDate())
                .updatedDate(loan.getUpdatedDate())
                .build();
    }
}