package com.example.spring.entity;

import jakarta.persistence.*;

public class Delivery {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "recipient_name")
    private String recipientName;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "zip_code")
    private String zipCode;

    @Column(name = "address")
    private String address;

    @Column(name = "address_detail")
    private String addressDetail;

    @Column(name = "delivery_memo", length = 200)
    private String deliveryMemo;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 40)
    private DeliveryStatus status;

    @Column(name = "tracking_number")
    private boolean trackingNumber;

    @Column(name = "courier_company")
    private boolean courierCompany;
}
