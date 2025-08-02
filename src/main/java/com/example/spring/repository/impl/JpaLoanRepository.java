package com.example.spring.repository.impl;

import com.example.spring.entity.Loan;
import com.example.spring.repository.LoanRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaLoanRepository extends JpaRepository<Loan, Long>, LoanRepository {
}
