package com.example.spring.service.impl;

import com.example.spring.dto.request.CreateMemberRequest;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

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
    @Cacheable(value = "members", key = "#id")
    public MemberResponse findMemberById(Long id) {
        log.debug("회원 조회 - ID: {}", id);
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Member", id));

        return MemberResponse.from(member);
    }

    @Override
    public List<MemberResponse> findMembersByName(String name) {
        return List.of();
    }

    @Override
    public List<MemberResponse> findMembersByMembershipType(MembershipType type) {
        return List.of();
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
