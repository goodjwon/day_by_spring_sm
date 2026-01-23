package com.example.spring.entity;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String method;

    private PaymentStatus status;

    private int amount;

    private String cardCompany;

    private String cardNumber;

    private int installmentMonth;
}
