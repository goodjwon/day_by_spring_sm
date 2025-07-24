package com.example.spring.repository;

import com.example.spring.entity.Member;

import java.util.List;
import java.util.Optional;

public interface MemberRepository {
    Member save(Member member);
    Optional<Member> findById(Long id);
    Optional<Member> findByEmail(String email);
    List<Member> findAll();

    default Member findByIdOrThrow(Long id){
        return findById(id).orElse(null);
    }

    default Member findByEmailOrThrow(String email){
        return findByEmail(email).orElse(null);
    }
}
