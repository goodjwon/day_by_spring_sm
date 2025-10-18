package com.example.patten.observer.extra;

import lombok.Data;

import java.time.LocalDateTime;


@Data
public class User {
    private Long id;
    private String email;
    private String name;
    private String phone;
    private String referralCode;
    private LocalDateTime createdAt;

    public User(String email, String name, String phone) {
        this.email = email;
        this.name = name;
        this.phone = phone;
    }

    public User(String email, String name, String phone, String referralCode) {
        this.email = email;
        this.name = name;
        this.phone = phone;
        this.referralCode = referralCode;
    }
}
