package com.example.spring.repository;

import com.example.spring.entity.Loan;

import java.util.List;
import java.util.Optional;

public interface LoanRepository {
    Loan save(Loan loan);
    Optional<Loan> findById(Long id);
    List<Loan> findAll();

    default Loan findByIdOrThrow(Long id){
        return findById(id).orElse(null);
    }
}
