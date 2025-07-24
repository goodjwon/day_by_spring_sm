package com.example.spring.repository.imple;

import com.example.spring.entity.Member;
import com.example.spring.repository.MemberRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaMemberRepository extends JpaRepository<Member, Long>, MemberRepository {
}
