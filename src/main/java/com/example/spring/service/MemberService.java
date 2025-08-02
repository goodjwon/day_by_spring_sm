package com.example.spring.service;

import com.example.spring.dto.request.CreateMemberRequest;
import com.example.spring.dto.response.MemberResponse;
import com.example.spring.entity.MembershipType;

import java.util.List;

public interface MemberService {

  public MemberResponse createMember(CreateMemberRequest request);

  public MemberResponse findMemberById(Long id);

  // todo 과제. (노멀)
  public List<MemberResponse> findMembersByName(String name);

  // todo 과제. (노멀)
  public List<MemberResponse> findMembersByMembershipType(MembershipType type);

  // todo 과제. (하드)
  public void upgradeMembership(Long memberId, MembershipType newType);

}
