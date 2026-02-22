package com.example.spring.domain.repository;

import com.example.spring.domain.model.Member;
import com.example.spring.domain.model.MembershipType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Member Repository - Spring Data JPA 기반
 *
 * JpaRepository: 기본 CRUD 제공
 * JpaSpecificationExecutor: 동적 쿼리 (Specification 패턴) 지원
 */
public interface MemberRepository extends JpaRepository<Member, Long>, JpaSpecificationExecutor<Member> {

    // ========== 이메일 관련 ==========

    Optional<Member> findByEmail(String email);

    boolean existsByEmail(String email);

    // ========== 멤버십 타입 조회 ==========

    List<Member> findByMembershipType(MembershipType membershipType);

    List<Member> findByMembershipTypeOrderByJoinDateDesc(MembershipType membershipType);

    // ========== 이름 검색 ==========

    List<Member> findByNameContaining(String name);

    List<Member> findByNameContainingIgnoreCase(String name);

    // ========== 복합 조건 검색 ==========

    List<Member> findByMembershipTypeAndNameContaining(MembershipType membershipType, String name);

    List<Member> findByMembershipTypeAndNameContainingIgnoreCase(MembershipType membershipType, String name);

    // ========== 날짜 범위 검색 ==========

    List<Member> findByJoinDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    List<Member> findByJoinDateAfter(LocalDateTime date);

    List<Member> findByJoinDateBefore(LocalDateTime date);

    // ========== 통계 ==========

    long countByMembershipType(MembershipType membershipType);

    // ========== 정렬 ==========

    List<Member> findAllByOrderByJoinDateDesc();

    List<Member> findAllByOrderByNameAsc();

    // ========== 편의 메서드 ==========

    default Member findMemberById(Long id) {
        return findById(id).orElse(null);
    }

    default List<Member> findPremiumMembers() {
        return findByMembershipType(MembershipType.PREMIUM);
    }

    default List<Member> findRegularMembers() {
        return findByMembershipType(MembershipType.REGULAR);
    }

    default List<Member> findSuspendedMembers() {
        return findByMembershipType(MembershipType.SUSPENDED);
    }
}