package com.example.spring.entity;

import jakarta.persistence.*;

public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "method")
    private PaymentMethod method;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 40)
    private PaymentStatus status;

    @Column(name = "amount", nullable = false)
    private int amount;

    @Column(name = "card_company")
    private String cardCompany;

    @Column(name = "card_number")
    private String cardNumber;

    @Column(name = "installment_month")
    private int installmentMonth;
}
