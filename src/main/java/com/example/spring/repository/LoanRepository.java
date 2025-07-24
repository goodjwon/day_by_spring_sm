package com.example.spring.repository;

import com.example.spring.entity.Loan;

import java.util.List;
import java.util.Optional;

public interface LoanRepository {
    Loan save(Loan loan);
    Optional<Loan> findById(Integer id);
    List<Loan> findAll();

    default Loan findByIdOrThrow(Integer id){
        return findById(id).orElse(null);
    }
}
