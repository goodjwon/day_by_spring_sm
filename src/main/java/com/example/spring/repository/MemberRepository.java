package com.example.spring.repository;

import com.example.spring.entity.Member;
import com.example.spring.entity.MembershipType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MemberRepository {
    Optional<Member> findById(Long id);
    List<Member> findAll();
    Member save(Member member);
    void deleteById(Long id);

    Optional<Member> findByEmail(String email);
    List<Member> findByMembershipType(MembershipType membershipType);
    List<Member> findByNameContaining(String name);
    boolean existsByEmail(String email);

    // 복합 조건 검색
    List<Member> findByMembershipTypeAndNameContaining(MembershipType membershipType, String name);

    // 날짜 범위 검색
    List<Member> findByJoinDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    default Member findMemberById(Long id) {
        return findById(id).orElse(null);
    }
}
