package com.example.spring.service.impl;

import com.example.spring.dto.request.CreateMemberRequest;
import com.example.spring.dto.request.UpdateMemberRequest;
import com.example.spring.dto.response.MemberResponse;
import com.example.spring.entity.Member;
import com.example.spring.entity.MembershipType;
import com.example.spring.event.MemberRegisteredEvent;
import com.example.spring.exception.DuplicateEmailException;
import com.example.spring.exception.EntityNotFoundException;
import com.example.spring.repository.MemberRepository;
import com.example.spring.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public MemberResponse createMember(CreateMemberRequest request) {
        log.info("회원 생성 요청 - 이메일: {}, 이름: {}", request.getEmail(), request.getName());

        //1. 이메일 중복 검증
        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException(request.getEmail());
        }

        //2. Member 엔티티 생성 (기본 맴버쉽 . 레귤러)
        Member member = Member.builder()
                .name(request.getName())
                .email(request.getEmail())
                .membershipType(request.getMembershipType() != null ?
                        request.getMembershipType() : MembershipType.REGULAR)
                .joinDate(LocalDateTime.now()) //3. 가일입 설정 (현재시간)
                .build();

        //3. 데이터저장
        Member savedMember = memberRepository.save(member);


        // 4. 회원가입 이벤트 발행 (환영 이메일 발송용)
        eventPublisher.publishEvent(new MemberRegisteredEvent(savedMember));

        log.info("회원 생성 완료 - ID: {}, 이메일: {}", savedMember.getId(), savedMember.getEmail());

        return MemberResponse.from(savedMember);
    }

    @Override
    public MemberResponse updateMember(Long id, UpdateMemberRequest request) {
        log.info("회원 정보 수정 요청 - ID: {}", id);
        //1. 기존 회원 정보
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Member", id));

        //2. 새로운 이름으로 갱신
        member.setName(request.getName());

        //1. 이메일 중복 검증
        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException(request.getEmail());
        }

        //3. 새로운 이메일로 갱신
        member.setEmail(request.getEmail());

        Member updatedMember = memberRepository.save(member);

        log.info("회원 정보 수정 완료 - ID: {}", updatedMember.getId());

        return MemberResponse.from(updatedMember);
    }


    @Override
    @Cacheable(value = "members", key = "#id")
    public MemberResponse findMemberById(Long id) {
        log.debug("회원 조회 - ID: {}", id);
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Member", id));

        return MemberResponse.from(member);
    }

    @Override
    public MemberResponse findMemberByIdWithoutCache(Long id) {
        log.debug("회원 조회 (캐시 미적용) - ID: {}", id);
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Member not found with id: ", id));

        return MemberResponse.from(member);
    }

    @Override
    public List<MemberResponse> findAllMembers() {
        List<Member> members = memberRepository.findAll();

        List<MemberResponse> results = new ArrayList<>();

        for (Member member : members) {
            MemberResponse.from(member);
            results.add(MemberResponse.from(member));
        }

        /*
         *   return memberRepository.findAll().stream()
         *                 .map(MemberResponse::from)
         *                 .collect(Collectors.toList());
         */

        return results;
    }

    @Override
    public List<MemberResponse> findAllMembersPage(Pageable pageable) {
        log.debug("전체 회원 목록 조회 - 페이지: {}, 크기: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        return memberRepository.findAll()
                .stream()
                .map(MemberResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    public List<MemberResponse> findMembersByName(String name) {
        log.debug("이름으로 회원 검색 - 검색어: {}", name);

        return memberRepository.findByNameContaining(name)
                .stream()
                .map(MemberResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    public List<MemberResponse> findMembersByMembershipType(MembershipType type) {
        log.debug("멤버쉽 타입으로 회원 조회: {}", type);
        return memberRepository.findByMembershipType(type)
                .stream()
                .map(MemberResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    public void upgradeMembership(Long memberId, MembershipType newType) {
        //0. 시작로그
        //1.회원 정보 조회
        //1.1 없을때 예외처리.
        //2.회원 등급 업그레이드
        //2.1. 최하위 등급이면 같은 등급으로 처리 하지 않음.
        //2.2. 최상위 등립이면 더이상 처리하지 않음.
        //3. 회원정보 저장(수정) save() upx
        //0. 끝로그


    }
}
