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
import com.example.spring.application.service.MemberServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * MemberService 단위 테스트
 * - Mock을 사용하여 비즈니스 로직에만 집중
 */
@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private MemberServiceImpl memberService;

    @Test
    @DisplayName("회원 생성 - 성공")
    void createMember_성공() {
        // Given
        CreateMemberRequest request = CreateMemberRequest.builder()
                .name("홍길동")
                .email("hong@example.com")
                .build();

        Member savedMember = Member.builder()
                .id(1L)
                .name("홍길동")
                .email("hong@example.com")
                .password("test-password")
                .role(Role.USER)
                .membershipType(MembershipType.REGULAR)
                .build();

        given(memberRepository.existsByEmail("hong@example.com")).willReturn(false);
        given(passwordEncoder.encode(anyString())).willReturn("encoded-password");
        given(memberRepository.save(any(Member.class))).willReturn(savedMember);

        // When
        MemberResponse response = memberService.createMember(request);

        // Then
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("홍길동");
        assertThat(response.getEmail()).isEqualTo("hong@example.com");
        assertThat(response.getMembershipType()).isEqualTo(MembershipType.REGULAR);

        // 이벤트 발행 확인
        ArgumentCaptor<MemberRegisteredEvent> eventCaptor = ArgumentCaptor.forClass(MemberRegisteredEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        MemberRegisteredEvent capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent.getMember().getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("회원 생성 - 이메일 중복 시 예외 발생")
    void createMember_이메일중복_예외발생() {
        // Given
        CreateMemberRequest request = CreateMemberRequest.builder()
                .name("홍길동")
                .email("duplicate@example.com")
                .build();

        given(memberRepository.existsByEmail("duplicate@example.com")).willReturn(true);

        // When & Then
        assertThatThrownBy(() -> memberService.createMember(request))
                .isInstanceOf(MemberException.DuplicateEmailException.class)
                .hasMessageContaining("duplicate@example.com");
    }

    @Test
    @DisplayName("회원 조회 - 성공")
    void findMemberById_성공() {
        // Given
        Long memberId = 1L;
        Member member = Member.builder()
                .id(memberId)
                .name("홍길동")
                .email("hong@example.com")
                .password("test-password")
                .role(Role.USER)
                .membershipType(MembershipType.REGULAR)
                .build();

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));

        // When
        MemberResponse response = memberService.findMemberById(memberId);

        // Then
        assertThat(response.getId()).isEqualTo(memberId);
        assertThat(response.getName()).isEqualTo("홍길동");
        assertThat(response.getEmail()).isEqualTo("hong@example.com");
    }

    @Test
    @DisplayName("회원 조회 - 존재하지 않는 회원 시 예외 발생")
    void findMemberById_존재하지않음_예외발생() {
        // Given
        Long nonExistentId = 999L;
        given(memberRepository.findById(nonExistentId)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> memberService.findMemberById(nonExistentId))
                .isInstanceOf(MemberException.MemberNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("회원 정보 수정 - 성공")
    void updateMember_성공() {
        // Given
        Long memberId = 1L;
        UpdateMemberRequest request = UpdateMemberRequest.builder()
                .name("김길동")
                .email("kim@example.com")
                .build();

        Member existingMember = Member.builder()
                .id(memberId)
                .name("홍길동")
                .email("hong@example.com")
                .password("test-password")
                .role(Role.USER)
                .membershipType(MembershipType.REGULAR)
                .build();

        Member updatedMember = Member.builder()
                .id(memberId)
                .name("김길동")
                .email("kim@example.com")
                .password("test-password")
                .role(Role.USER)
                .membershipType(MembershipType.REGULAR)
                .build();


        given(memberRepository.findById(memberId)).willReturn(Optional.of(existingMember));
        given(memberRepository.existsByEmail("kim@example.com")).willReturn(false);
        given(memberRepository.save(any(Member.class))).willReturn(updatedMember);

        // When
        MemberResponse response = memberService.updateMember(memberId, request);

        // Then
        assertThat(response.getName()).isEqualTo("김길동");
        assertThat(response.getEmail()).isEqualTo("kim@example.com");
    }

    @Test
    @DisplayName("멤버십 업그레이드 - REGULAR에서 PREMIUM으로 성공")
    void upgradeMembership_Regular에서Premium_성공() {
        // Given
        Long memberId = 1L;
        Member member = Member.builder()
                .id(memberId)
                .name("홍길동")
                .email("hong@example.com")
                .password("test-password")
                .role(Role.USER)
                .membershipType(MembershipType.REGULAR)
                .build();

        Member upgradedMember = Member.builder()
                .id(memberId)
                .name("홍길동")
                .email("hong@example.com")
                .password("test-password")
                .role(Role.USER)
                .membershipType(MembershipType.PREMIUM)
                .build();


        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(memberRepository.save(any(Member.class))).willReturn(upgradedMember);

        // When
        memberService.upgradeMembership(memberId, MembershipType.PREMIUM);

        // Then
        ArgumentCaptor<MembershipUpgradedEvent> eventCaptor =
                ArgumentCaptor.forClass(MembershipUpgradedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        MembershipUpgradedEvent capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent.getMember().getId()).isEqualTo(memberId);
        assertThat(capturedEvent.getPreviousType()).isEqualTo(MembershipType.REGULAR);
        assertThat(capturedEvent.getNewType()).isEqualTo(MembershipType.PREMIUM);
    }

    @Test
    @DisplayName("멤버십 업그레이드 - SUSPENDED 회원은 업그레이드 불가")
    void upgradeMembership_Suspended회원_업그레이드불가() {
        // Given
        Long memberId = 1L;
        Member suspendedMember = Member.builder()
                .id(memberId)
                .name("홍길동")
                .email("hong@example.com")
                .password("test-password")
                .role(Role.USER)
                .membershipType(MembershipType.SUSPENDED)
                .build();

        given(memberRepository.findById(memberId)).willReturn(Optional.of(suspendedMember));

        // When & Then
        assertThatThrownBy(() -> memberService.upgradeMembership(memberId, MembershipType.PREMIUM))
                .isInstanceOf(MembershipUpgradeException.class);
    }

    @Test
    @DisplayName("이메일 중복 검증 - 중복 없음")
    void validateEmailDuplicate_중복없음_true반환() {
        // Given
        String email = "new@example.com";
        given(memberRepository.existsByEmail(email)).willReturn(false);

        // When
        boolean result = memberService.validateEmailDuplicate(email);

        // Then
        assertThat(result).isTrue(); // 사용 가능
    }

    @Test
    @DisplayName("이메일 중복 검증 - 중복 있음")
    void validateEmailDuplicate_중복있음_false반환() {
        // Given
        String email = "existing@example.com";
        given(memberRepository.existsByEmail(email)).willReturn(true);

        // When
        boolean result = memberService.validateEmailDuplicate(email);

        // Then
        assertThat(result).isFalse(); // 사용 불가
    }

    @Test
    @DisplayName("회원 대여 제한 정보 조회 - REGULAR 회원")
    void getMemberLoanLimitInfo_Regular회원_제한정보반환() {
        // Given
        Long memberId = 1L;
        Member member = Member.builder()
                .id(memberId)
                .name("홍길동")
                .email("hong@example.com")
                .password("test-password")
                .role(Role.USER)
                .membershipType(MembershipType.REGULAR)
                .build();

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(loanRepository.findByMemberIdAndReturnDateIsNull(memberId)).willReturn(List.of()); // 대여 중인 도서 없음

        // When
        MemberLoanLimitInfo info = memberService.getMemberLoanLimitInfo(memberId);

        // Then
        assertThat(info.getMemberId()).isEqualTo(memberId);
        assertThat(info.getMembershipType()).isEqualTo(MembershipType.REGULAR);
        assertThat(info.getMaxLoanCount()).isEqualTo(5);
        assertThat(info.getCurrentLoanCount()).isEqualTo(0);
        assertThat(info.getRemainingLoanCount()).isEqualTo(5);
        assertThat(info.isCanLoan()).isTrue();
    }
}