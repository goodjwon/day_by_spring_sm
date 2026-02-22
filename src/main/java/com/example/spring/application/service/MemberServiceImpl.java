package com.example.spring.application.service;

import com.example.spring.application.dto.request.CreateMemberRequest;
import com.example.spring.application.dto.request.UpdateMemberRequest;
import com.example.spring.application.dto.response.MemberLoanLimitInfo;
import com.example.spring.application.dto.response.MemberResponse;
import com.example.spring.domain.model.Member;
import com.example.spring.domain.model.MembershipType;
import com.example.spring.domain.model.Role;
import com.example.spring.domain.event.MemberRegisteredEvent;
import com.example.spring.domain.event.MembershipUpgradedEvent;
import com.example.spring.exception.MemberException;
import com.example.spring.exception.MembershipUpgradeException;
import com.example.spring.domain.repository.LoanRepository;
import com.example.spring.domain.repository.MemberRepository;
import com.example.spring.application.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 회원 관리 서비스 구현체
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final LoanRepository loanRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public MemberResponse createMember(CreateMemberRequest request) {

        // 1. 이메일 중복 검증
        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new MemberException.DuplicateEmailException(request.getEmail());
        }

        // 2. Member 엔티티 생성
        // 기본 비밀번호는 "1234"로 설정 (추후 변경 필요)
        // 기본 역할은 USER로 설정
        Member member = Member.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode("1234"))
                .role(Role.USER)
                .membershipType(request.getMembershipType() != null ?
                        request.getMembershipType() : MembershipType.REGULAR)
                .joinDate(LocalDateTime.now())
                .build();

        // 3. 데이터베이스 저장
        Member savedMember = memberRepository.save(member);

        // 4. 회원가입 이벤트 발행 (환영 이메일 발송용)
        eventPublisher.publishEvent(new MemberRegisteredEvent(savedMember));
        return MemberResponse.from(savedMember);
    }

    @Override
    @Transactional
    @CacheEvict(value = "members", key = "#id")
    public MemberResponse updateMember(Long id, UpdateMemberRequest request) {

        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new MemberException.MemberNotFoundException(id));

        // 이메일 변경 시 중복 체크
        if (StringUtils.hasText(request.getEmail()) &&
                !request.getEmail().equals(member.getEmail())) {

            if (memberRepository.existsByEmail(request.getEmail())) {
                throw new MemberException.DuplicateEmailException(request.getEmail());
            }
            member.updateEmail(request.getEmail());
        }

        // 이름 변경
        if (StringUtils.hasText(request.getName())) {
            member.updateName(request.getName());
        }

        Member updatedMember = memberRepository.save(member);
        return MemberResponse.from(updatedMember);
    }

    @Override
    @Cacheable(value = "members", key = "#id")
    public MemberResponse findMemberById(Long id) {

        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new MemberException.MemberNotFoundException(id));

        return MemberResponse.from(member);
    }

    @Override
    public Page<MemberResponse> findAllMembers(Pageable pageable) {

        List<MemberResponse> responses = memberRepository.findAll()
                .stream()
                .map(MemberResponse::from)
                .collect(Collectors.toList());

        return new PageImpl<>(responses, pageable, responses.size());
    }

    @Override
    @Transactional
    @CacheEvict(value = "members", key = "#id")
    public void deleteMember(Long id) {

        if (memberRepository.findById(id).isEmpty()) {
            throw new MemberException.MemberNotFoundException(id);
        }

        // TODO: 대여 중인 도서가 있는지 확인하는 로직 추가 필요
        memberRepository.deleteById(id);
    }

    @Override
    public List<MemberResponse> findMembersByName(String name) {

        return memberRepository.findByNameContaining(name)
                .stream()
                .map(MemberResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    public List<MemberResponse> findMembersByMembershipType(MembershipType type) {

        return memberRepository.findByMembershipType(type)
                .stream()
                .map(MemberResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @CacheEvict(value = "members", key = "#memberId")
    public void upgradeMembership(Long memberId, MembershipType newType) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException.MemberNotFoundException(memberId));

        MembershipType currentType = member.getMembershipType();

        // 업그레이드 가능성 검증
        if (!isUpgradeAllowed(currentType, newType)) {
            throw new MembershipUpgradeException(currentType, newType);
        }

        // 멤버십 업그레이드
        member.upgradeMembership(newType);
        Member upgradedMember = memberRepository.save(member);

        // 멤버십 업그레이드 이벤트 발행
        eventPublisher.publishEvent(new MembershipUpgradedEvent(upgradedMember, currentType, newType));
    }

    @Override
    @Transactional
    @CacheEvict(value = "members", key = "#memberId")
    public void promoteToAdmin(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException.MemberNotFoundException(memberId));

        if (member.isAdmin()) {
            return;
        }

        member.changeRole(Role.ADMIN);
        memberRepository.save(member);
    }

    @Override
    public boolean validateEmailDuplicate(String email) {
        boolean exists = memberRepository.existsByEmail(email);
        return !exists; // true: 사용가능, false: 중복
    }

    @Override
    public MemberLoanLimitInfo getMemberLoanLimitInfo(Long memberId) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException.MemberNotFoundException(memberId));

        // 현재 대여 중인 도서 수 조회
        int currentLoanCount = loanRepository.findByMemberIdAndReturnDateIsNull(memberId).size();

        return MemberLoanLimitInfo.of(
                memberId,
                member.getName(),
                member.getMembershipType(),
                currentLoanCount
        );
    }

    /**
     * 멤버십 업그레이드 가능 여부 확인
     */
    private boolean isUpgradeAllowed(MembershipType currentType, MembershipType newType) {
        // SUSPENDED는 업그레이드 불가
        if (currentType == MembershipType.SUSPENDED) {
            return false;
        }

        // 동일한 타입으로는 업그레이드 불가
        if (currentType == newType) {
            return false;
        }

        // REGULAR -> PREMIUM만 허용 (다운그레이드는 별도 처리 필요)
        return currentType == MembershipType.REGULAR && newType == MembershipType.PREMIUM;
    }
}