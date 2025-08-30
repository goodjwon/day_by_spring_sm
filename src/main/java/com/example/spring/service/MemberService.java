package com.example.spring.service;

import com.example.spring.dto.request.CreateMemberRequest;
import com.example.spring.dto.request.UpdateMemberRequest;
import com.example.spring.dto.response.MemberResponse;
import com.example.spring.entity.MembershipType;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MemberService {

    MemberResponse createMember(CreateMemberRequest request);
    MemberResponse updateMember(Long id, UpdateMemberRequest request);


    MemberResponse findMemberById(Long id);
    MemberResponse findMemberByIdWithoutCache(Long id);
    List<MemberResponse> findAllMembers();
    List<MemberResponse> findAllMembersPage(Pageable pageable);


    // 회원 검색
    List<MemberResponse> findMembersByName(String name);
    List<MemberResponse> findMembersByMembershipType(MembershipType type);

    // 비지시스로직
    void upgradeMembership(Long memberId, MembershipType newType);


}
