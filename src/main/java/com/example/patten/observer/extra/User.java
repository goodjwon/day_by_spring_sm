package com.example.patten.observer.extra;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class User {

    private Long id;
    private String email;
    private String name;
    private String phone;
    private LocalDateTime createdAt;

    public User(String email, String name, String phone) {
        this.email = email;
        this.name = name;
        this.phone = phone;
        this.createdAt = LocalDateTime.now();
    }
}
