package com.example.spring.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import java.time.LocalDateTime;

@Entity
@Table(name = "member", indexes = {
        @Index(name = "idx_member_email", columnList = "email"),
        @Index(name = "idx_member_name", columnList = "name"),
        @Index(name = "idx_member_role", columnList = "role")
})
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"password"})
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(name = "membership_type")
    private MembershipType membershipType;

    @Column(name = "join_date")
    private LocalDateTime joinDate;

    /**
     * 회원 이름 변경
     */
    public void updateName(String newName) {
        this.name = newName;
    }

    /**
     * 비밀번호 변경
     */
    public void changePassword(String newPassword) {
        this.password = newPassword;
    }

    /**
     * 이메일 변경
     */
    public void updateEmail(String newEmail) {
        this.email = newEmail;
    }

    /**
     * 역할 변경
     */
    public void changeRole(Role newRole) {
        this.role = newRole;
    }

    /**
     * 멤버십 업그레이드
     */
    public void upgradeMembership(MembershipType newType) {
        this.membershipType = newType;
    }

    /**
     * 관리자 여부 확인
     */
    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

    /**
     * 프리미엄 회원 여부 확인
     */
    public boolean isPremium() {
        return membershipType == MembershipType.PREMIUM;
    }
}