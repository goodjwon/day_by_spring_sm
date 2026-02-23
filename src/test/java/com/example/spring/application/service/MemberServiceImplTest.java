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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MemberServiceImpl 테스트")
class MemberServiceImplTest {

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


    private Member testMember;
    private CreateMemberRequest createRequest;
    private UpdateMemberRequest updateRequest;

    @BeforeEach
    void setUp() {
        testMember = Member.builder()
                .id(1L)
                .name("홍길동")
                .email("hong@example.com")
                .password("test-password")
                .role(Role.USER)
                .membershipType(MembershipType.REGULAR)
                .joinDate(LocalDateTime.now())
                .build();

        createRequest = CreateMemberRequest.builder()
                .name("김철수")
                .email("kim@example.com")
                .membershipType(MembershipType.REGULAR)
                .build();

        updateRequest = UpdateMemberRequest.builder()
                .name("김영희")
                .email("kim.new@example.com")
                .build();
    }

    @Nested
    @DisplayName("회원 생성")
    class CreateMemberTest {

        @Test
        @DisplayName("정상적인 회원 생성")
        void createMember_Success() {
            // given
            given(memberRepository.existsByEmail(createRequest.getEmail())).willReturn(false);
            given(passwordEncoder.encode(anyString())).willReturn("encoded-password");
            given(memberRepository.save(any(Member.class))).willReturn(testMember);

            // when
            MemberResponse response = memberService.createMember(createRequest);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(testMember.getId());
            assertThat(response.getName()).isEqualTo(testMember.getName());
            assertThat(response.getEmail()).isEqualTo(testMember.getEmail());
            assertThat(response.getMembershipType()).isEqualTo(testMember.getMembershipType());

            verify(memberRepository).existsByEmail(createRequest.getEmail());
            verify(memberRepository).save(any(Member.class));
            verify(eventPublisher).publishEvent(any(MemberRegisteredEvent.class));
        }

        @Test
        @DisplayName("이메일 중복으로 회원 생성 실패")
        void createMember_DuplicateEmail_ThrowsException() {
            // given
            given(memberRepository.existsByEmail(createRequest.getEmail())).willReturn(true);

            // when & then
            assertThatThrownBy(() -> memberService.createMember(createRequest))
                    .isInstanceOf(MemberException.DuplicateEmailException.class);

            verify(memberRepository).existsByEmail(createRequest.getEmail());
            verify(memberRepository, never()).save(any(Member.class));
            verify(eventPublisher, never()).publishEvent(any());
        }

        @Test
        @DisplayName("멤버십 타입 기본값 설정")
        void createMember_DefaultMembershipType() {
            // given
            CreateMemberRequest requestWithoutMembership = CreateMemberRequest.builder()
                    .name("박민수")
                    .email("park@example.com")
                    .build(); // membershipType 없음

            given(memberRepository.existsByEmail(requestWithoutMembership.getEmail())).willReturn(false);
            given(passwordEncoder.encode(anyString())).willReturn("encoded-password");
            given(memberRepository.save(any(Member.class))).willReturn(testMember);

            // when
            memberService.createMember(requestWithoutMembership);

            // then
            verify(memberRepository).save(argThat(member ->
                    member.getMembershipType() == MembershipType.REGULAR));
        }
    }

    @Nested
    @DisplayName("회원 조회")
    class FindMemberTest {

        @Test
        @DisplayName("ID로 회원 조회 성공")
        void findMemberById_Success() {
            // given
            given(memberRepository.findById(1L)).willReturn(Optional.of(testMember));

            // when
            MemberResponse response = memberService.findMemberById(1L);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(testMember.getId());
            assertThat(response.getName()).isEqualTo(testMember.getName());
            assertThat(response.getEmail()).isEqualTo(testMember.getEmail());

            verify(memberRepository).findById(1L);
        }

        @Test
        @DisplayName("존재하지 않는 회원 조회")
        void findMemberById_NotFound_ThrowsException() {
            // given
            given(memberRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> memberService.findMemberById(999L))
                    .isInstanceOf(MemberException.MemberNotFoundException.class)
                    .hasMessageContaining("999");

            verify(memberRepository).findById(999L);
        }

        @Test
        @DisplayName("이름으로 회원 검색")
        void findMembersByName_Success() {
            // given
            List<Member> members = List.of(testMember);
            given(memberRepository.findByNameContaining("홍")).willReturn(members);

            // when
            List<MemberResponse> responses = memberService.findMembersByName("홍");

            // then
            assertThat(responses).hasSize(1);
            assertThat(responses.get(0).getName()).contains("홍");

            verify(memberRepository).findByNameContaining("홍");
        }

        @Test
        @DisplayName("멤버십 타입별 회원 조회")
        void findMembersByMembershipType_Success() {
            // given
            List<Member> members = List.of(testMember);
            given(memberRepository.findByMembershipType(MembershipType.REGULAR)).willReturn(members);

            // when
            List<MemberResponse> responses = memberService.findMembersByMembershipType(MembershipType.REGULAR);

            // then
            assertThat(responses).hasSize(1);
            assertThat(responses.get(0).getMembershipType()).isEqualTo(MembershipType.REGULAR);

            verify(memberRepository).findByMembershipType(MembershipType.REGULAR);
        }
    }

    @Nested
    @DisplayName("회원 정보 수정")
    class UpdateMemberTest {

        @Test
        @DisplayName("회원 정보 수정 성공")
        void updateMember_Success() {
            // given
            given(memberRepository.findById(1L)).willReturn(Optional.of(testMember));
            given(memberRepository.existsByEmail(updateRequest.getEmail())).willReturn(false);
            given(memberRepository.save(any(Member.class))).willReturn(testMember);

            // when
            MemberResponse response = memberService.updateMember(1L, updateRequest);

            // then
            assertThat(response).isNotNull();
            verify(memberRepository).findById(1L);
            verify(memberRepository).existsByEmail(updateRequest.getEmail());
            verify(memberRepository).save(any(Member.class));
        }

        @Test
        @DisplayName("이메일 중복으로 회원 정보 수정 실패")
        void updateMember_DuplicateEmail_ThrowsException() {
            // given
            given(memberRepository.findById(1L)).willReturn(Optional.of(testMember));
            given(memberRepository.existsByEmail(updateRequest.getEmail())).willReturn(true);

            // when & then
            assertThatThrownBy(() -> memberService.updateMember(1L, updateRequest))
                    .isInstanceOf(MemberException.DuplicateEmailException.class);

            verify(memberRepository).findById(1L);
            verify(memberRepository).existsByEmail(updateRequest.getEmail());
            verify(memberRepository, never()).save(any(Member.class));
        }

        @Test
        @DisplayName("존재하지 않는 회원 수정")
        void updateMember_NotFound_ThrowsException() {
            // given
            given(memberRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> memberService.updateMember(999L, updateRequest))
                    .isInstanceOf(MemberException.MemberNotFoundException.class);

            verify(memberRepository).findById(999L);
            verify(memberRepository, never()).save(any(Member.class));
        }
    }

    @Nested
    @DisplayName("회원 삭제")
    class DeleteMemberTest {

        @Test
        @DisplayName("회원 삭제 성공")
        void deleteMember_Success() {
            // given
            given(memberRepository.findById(1L)).willReturn(Optional.of(testMember));

            // when
            memberService.deleteMember(1L);

            // then
            verify(memberRepository).findById(1L);
            verify(memberRepository).deleteById(1L);
        }

        @Test
        @DisplayName("존재하지 않는 회원 삭제")
        void deleteMember_NotFound_ThrowsException() {
            // given
            given(memberRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> memberService.deleteMember(999L))
                    .isInstanceOf(MemberException.MemberNotFoundException.class);

            verify(memberRepository).findById(999L);
            verify(memberRepository, never()).deleteById(anyLong());
        }
    }

    @Nested
    @DisplayName("멤버십 업그레이드")
    class UpgradeMembershipTest {

        @Test
        @DisplayName("REGULAR에서 PREMIUM으로 업그레이드 성공")
        void upgradeMembership_RegularToPremium_Success() {
            // given
            Member regularMember = Member.builder()
                    .id(1L)
                    .name("홍길동")
                    .email("hong@example.com")
                    .password("test-password")
                    .role(Role.USER)
                    .membershipType(MembershipType.REGULAR)
                    .joinDate(LocalDateTime.now())
                    .build();

            given(memberRepository.findById(1L)).willReturn(Optional.of(regularMember));
            given(memberRepository.save(any(Member.class))).willReturn(regularMember);

            // when
            memberService.upgradeMembership(1L, MembershipType.PREMIUM);

            // then
            verify(memberRepository).findById(1L);
            verify(memberRepository).save(argThat(member ->
                    member.getMembershipType() == MembershipType.PREMIUM));
            verify(eventPublisher).publishEvent(any(MembershipUpgradedEvent.class));
        }

        @Test
        @DisplayName("SUSPENDED 회원의 멤버십 업그레이드 실패")
        void upgradeMembership_SuspendedMember_ThrowsException() {
            // given
            Member suspendedMember = Member.builder()
                    .id(1L)
                    .name("정지회원")
                    .email("suspended@example.com")
                    .password("test-password")
                    .role(Role.USER)
                    .membershipType(MembershipType.SUSPENDED)
                    .joinDate(LocalDateTime.now())
                    .build();

            given(memberRepository.findById(1L)).willReturn(Optional.of(suspendedMember));

            // when & then
            assertThatThrownBy(() -> memberService.upgradeMembership(1L, MembershipType.PREMIUM))
                    .isInstanceOf(MembershipUpgradeException.class);

            verify(memberRepository).findById(1L);
            verify(memberRepository, never()).save(any(Member.class));
            verify(eventPublisher, never()).publishEvent(any());
        }

        @Test
        @DisplayName("동일한 타입으로 업그레이드 시도 시 실패")
        void upgradeMembership_SameType_ThrowsException() {
            // given
            given(memberRepository.findById(1L)).willReturn(Optional.of(testMember));

            // when & then
            assertThatThrownBy(() -> memberService.upgradeMembership(1L, MembershipType.REGULAR))
                    .isInstanceOf(MembershipUpgradeException.class);

            verify(memberRepository).findById(1L);
            verify(memberRepository, never()).save(any(Member.class));
            verify(eventPublisher, never()).publishEvent(any());
        }
    }

    @Nested
    @DisplayName("이메일 검증")
    class EmailValidationTest {

        @Test
        @DisplayName("사용 가능한 이메일")
        void validateEmailDuplicate_Available_ReturnsTrue() {
            // given
            given(memberRepository.existsByEmail("new@example.com")).willReturn(false);

            // when
            boolean result = memberService.validateEmailDuplicate("new@example.com");

            // then
            assertThat(result).isTrue();
            verify(memberRepository).existsByEmail("new@example.com");
        }

        @Test
        @DisplayName("중복된 이메일")
        void validateEmailDuplicate_Duplicate_ReturnsFalse() {
            // given
            given(memberRepository.existsByEmail("existing@example.com")).willReturn(true);

            // when
            boolean result = memberService.validateEmailDuplicate("existing@example.com");

            // then
            assertThat(result).isFalse();
            verify(memberRepository).existsByEmail("existing@example.com");
        }
    }

    @Nested
    @DisplayName("대여 제한 정보")
    class LoanLimitTest {

        @Test
        @DisplayName("REGULAR 회원 대여 제한 정보 조회")
        void getMemberLoanLimitInfo_RegularMember_Success() {
            // given
            given(memberRepository.findById(1L)).willReturn(Optional.of(testMember));
            given(loanRepository.findByMemberIdAndReturnDateIsNull(1L)).willReturn(List.of());

            // when
            MemberLoanLimitInfo info = memberService.getMemberLoanLimitInfo(1L);

            // then
            assertThat(info).isNotNull();
            assertThat(info.getMemberId()).isEqualTo(1L);
            assertThat(info.getMemberName()).isEqualTo("홍길동");
            assertThat(info.getMembershipType()).isEqualTo(MembershipType.REGULAR);
            assertThat(info.getMaxLoanCount()).isEqualTo(5);
            assertThat(info.getCurrentLoanCount()).isEqualTo(0);
            assertThat(info.getRemainingLoanCount()).isEqualTo(5);
            assertThat(info.isCanLoan()).isTrue();

            verify(memberRepository).findById(1L);
            verify(loanRepository).findByMemberIdAndReturnDateIsNull(1L);
        }

        @Test
        @DisplayName("PREMIUM 회원 대여 제한 정보 조회")
        void getMemberLoanLimitInfo_PremiumMember_Success() {
            // given
            Member premiumMember = Member.builder()
                    .id(2L)
                    .name("VIP회원")
                    .email("vip@example.com")
                    .password("test-password")
                    .role(Role.USER)
                    .membershipType(MembershipType.PREMIUM)
                    .joinDate(LocalDateTime.now())
                    .build();

            given(memberRepository.findById(2L)).willReturn(Optional.of(premiumMember));
            given(loanRepository.findByMemberIdAndReturnDateIsNull(2L)).willReturn(List.of());

            // when
            MemberLoanLimitInfo info = memberService.getMemberLoanLimitInfo(2L);

            // then
            assertThat(info.getMaxLoanCount()).isEqualTo(10);
            assertThat(info.getCurrentLoanCount()).isEqualTo(0);
            assertThat(info.getRemainingLoanCount()).isEqualTo(10);
            assertThat(info.isCanLoan()).isTrue();
        }

        @Test
        @DisplayName("SUSPENDED 회원은 대여 불가")
        void getMemberLoanLimitInfo_SuspendedMember_CannotLoan() {
            // given
            Member suspendedMember = Member.builder()
                    .id(3L)
                    .name("정지회원")
                    .email("suspended@example.com")
                    .password("test-password")
                    .role(Role.USER)
                    .membershipType(MembershipType.SUSPENDED)
                    .joinDate(LocalDateTime.now())
                    .build();

            given(memberRepository.findById(3L)).willReturn(Optional.of(suspendedMember));
            given(loanRepository.findByMemberIdAndReturnDateIsNull(3L)).willReturn(List.of());

            // when
            MemberLoanLimitInfo info = memberService.getMemberLoanLimitInfo(3L);

            // then
            assertThat(info.getMaxLoanCount()).isEqualTo(0);
            assertThat(info.isCanLoan()).isFalse();
        }
    }

    @Nested
    @DisplayName("전체 회원 조회")
    class FindAllMembersTest {

        @Test
        @DisplayName("페이지네이션을 통한 전체 회원 조회")
        void findAllMembers_WithPagination_Success() {
            // given
            List<Member> members = List.of(testMember);
            Pageable pageable = PageRequest.of(0, 20);
            given(memberRepository.findAll()).willReturn(members);

            // when
            Page<MemberResponse> responses = memberService.findAllMembers(pageable);

            // then
            assertThat(responses.getContent()).hasSize(1);
            assertThat(responses.getContent().get(0).getId()).isEqualTo(testMember.getId());

            verify(memberRepository).findAll();
        }
    }

    @Nested
    @DisplayName("관리자 승격")
    class PromoteToAdminTest {

        @Test
        @DisplayName("일반 회원을 관리자 승격")
        void promoteToAdmin_UserToAdmin_Success() {
            // given
            given(memberRepository.findById(1L)).willReturn(Optional.of(testMember));
            given(memberRepository.save(any(Member.class))).willAnswer(invocation -> invocation.getArgument(0));

            // when
            memberService.promoteToAdmin(1L);

            // then
            verify(memberRepository).save(argThat(member -> member.getRole() == Role.ADMIN));
        }

        @Test
        @DisplayName("이미 관리자인 경우 저장 생략")
        void promoteToAdmin_AlreadyAdmin_NoSave() {
            // given
            Member adminMember = Member.builder()
                    .id(1L)
                    .name("관리자")
                    .email("admin@example.com")
                    .password("encoded")
                    .role(Role.ADMIN)
                    .membershipType(MembershipType.REGULAR)
                    .joinDate(LocalDateTime.now())
                    .build();
            given(memberRepository.findById(1L)).willReturn(Optional.of(adminMember));

            // when
            memberService.promoteToAdmin(1L);

            // then
            verify(memberRepository, never()).save(any(Member.class));
        }
    }
}