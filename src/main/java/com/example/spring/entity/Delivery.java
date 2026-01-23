package com.example.spring.entity;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

public class Delivery {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String recipientName;

    private String phoneNumber;

    private String zipCode;

    private String address;

    private String addressDetail;

    private String deliveryMemo;

    private String status;

    private boolean trackingNumber;

    private boolean courierCompany;
}
