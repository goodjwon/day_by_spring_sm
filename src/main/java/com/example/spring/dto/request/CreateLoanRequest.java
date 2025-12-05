package com.example.spring.dto.request;

import com.example.spring.entity.Book;
import com.example.spring.entity.LoanStatus;
import com.example.spring.entity.Member;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateLoanRequest {

    private Member member;

    private Book book;

    private LoanStatus status;

    private BigDecimal overdueFee;
}
