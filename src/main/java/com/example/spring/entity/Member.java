package com.example.spring.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "member")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Member {
    // id, name, email, membership_type, join_date)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(name = "membership_type")
    private MembershipType membershipType;

    @Column(name = "join_date")
    private LocalDateTime joinDate;

    public void upgradeMembership(MembershipType targetType) {
        this.membershipType = targetType;
    }

    public void updateName(String name) {
        this.name = name;
    }
}
