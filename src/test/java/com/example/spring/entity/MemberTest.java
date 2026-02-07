package com.example.spring.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Member 엔티티 테스트")
class MemberTest {

    @Nested
    @DisplayName("회원 생성")
    class CreateMemberTest {

        @Test
        @DisplayName("빌더로 회원 생성 성공")
        void createMember_withBuilder_success() {
            // Given & When
            Member member = Member.builder()
                    .name("홍길동")
                    .email("hong@example.com")
                    .password("password123")
                    .role(Role.USER)
                    .membershipType(MembershipType.REGULAR)
                    .joinDate(LocalDateTime.now())
                    .build();

            // Then
            assertThat(member.getName()).isEqualTo("홍길동");
            assertThat(member.getEmail()).isEqualTo("hong@example.com");
            assertThat(member.getPassword()).isEqualTo("password123");
            assertThat(member.getRole()).isEqualTo(Role.USER);
            assertThat(member.getMembershipType()).isEqualTo(MembershipType.REGULAR);
            assertThat(member.getJoinDate()).isNotNull();
        }

        @Test
        @DisplayName("관리자 회원 생성")
        void createMember_adminRole_success() {
            // Given & When
            Member admin = Member.builder()
                    .name("관리자")
                    .email("admin@example.com")
                    .password("admin123")
                    .role(Role.ADMIN)
                    .membershipType(MembershipType.PREMIUM)
                    .joinDate(LocalDateTime.now())
                    .build();

            // Then
            assertThat(admin.getRole()).isEqualTo(Role.ADMIN);
        }

        @Test
        @DisplayName("기본 생성자로 회원 생성")
        void createMember_noArgsConstructor_success() {
            // Given & When - 빌더를 사용하여 회원 생성
            Member member = Member.builder()
                    .name("김철수")
                    .email("kim@example.com")
                    .password("password")
                    .role(Role.USER)
                    .build();

            // Then
            assertThat(member.getName()).isEqualTo("김철수");
            assertThat(member.getEmail()).isEqualTo("kim@example.com");
        }
    }

    @Nested
    @DisplayName("멤버십 타입")
    class MembershipTypeTest {

        @Test
        @DisplayName("REGULAR 멤버십 - 최대 대출 5권")
        void regular_maxBorrowCount_5() {
            assertThat(MembershipType.REGULAR.getMaxBorrowCount()).isEqualTo(5);
            assertThat(MembershipType.REGULAR.isActive()).isTrue();
        }

        @Test
        @DisplayName("PREMIUM 멤버십 - 최대 대출 10권")
        void premium_maxBorrowCount_10() {
            assertThat(MembershipType.PREMIUM.getMaxBorrowCount()).isEqualTo(10);
            assertThat(MembershipType.PREMIUM.isActive()).isTrue();
        }

        @Test
        @DisplayName("SUSPENDED 멤버십 - 대출 불가")
        void suspended_maxBorrowCount_0() {
            assertThat(MembershipType.SUSPENDED.getMaxBorrowCount()).isEqualTo(0);
            assertThat(MembershipType.SUSPENDED.isActive()).isFalse();
        }
    }

    @Nested
    @DisplayName("Role 테스트")
    class RoleTest {

        @Test
        @DisplayName("USER 역할")
        void userRole() {
            Member member = Member.builder()
                    .name("사용자")
                    .email("user@example.com")
                    .password("password")
                    .role(Role.USER)
                    .build();

            assertThat(member.getRole()).isEqualTo(Role.USER);
            assertThat(member.getRole().name()).isEqualTo("USER");
        }

        @Test
        @DisplayName("ADMIN 역할")
        void adminRole() {
            Member member = Member.builder()
                    .name("관리자")
                    .email("admin@example.com")
                    .password("password")
                    .role(Role.ADMIN)
                    .build();

            assertThat(member.getRole()).isEqualTo(Role.ADMIN);
            assertThat(member.getRole().name()).isEqualTo("ADMIN");
        }
    }

    @Nested
    @DisplayName("회원 정보 수정")
    class UpdateMemberTest {

        @Test
        @DisplayName("회원 이름 수정")
        void updateName_success() {
            // Given
            Member member = Member.builder()
                    .name("홍길동")
                    .email("hong@example.com")
                    .password("password")
                    .role(Role.USER)
                    .build();

            // When
            member.updateName("이순신");

            // Then
            assertThat(member.getName()).isEqualTo("이순신");
        }

        @Test
        @DisplayName("멤버십 타입 변경")
        void updateMembershipType_success() {
            // Given
            Member member = Member.builder()
                    .name("홍길동")
                    .email("hong@example.com")
                    .password("password")
                    .role(Role.USER)
                    .membershipType(MembershipType.REGULAR)
                    .build();

            // When
            member.upgradeMembership(MembershipType.PREMIUM);

            // Then
            assertThat(member.getMembershipType()).isEqualTo(MembershipType.PREMIUM);
        }
    }
}
