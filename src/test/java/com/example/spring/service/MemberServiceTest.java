package com.example.spring.service;

import com.example.spring.dto.request.CreateMemberRequest;
import com.example.spring.dto.request.UpdateMemberRequest;
import com.example.spring.dto.response.MemberResponse;
import com.example.spring.entity.Member;
import com.example.spring.entity.MembershipType;
import com.example.spring.event.MemberRegisteredEvent;
import com.example.spring.exception.DuplicateEmailException;
import com.example.spring.repository.MemberRepository;
import com.example.spring.service.impl.MemberServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private MemberServiceImpl memberService;

    @Mock
    private ApplicationEventPublisher eventPublisher;


    @Test
    @DisplayName("회원 생성 - 성공")
    void createMember_성공() {
        //Given
        CreateMemberRequest request = CreateMemberRequest.builder()
                .name("홍길동")
                .email("hong@example.com")
                .build();

        Member savedMember = Member.builder()
                .id(1L)
                .name("홍길동")
                .email("hong@example.com")
                .membershipType(MembershipType.REGULAR)
                .build();

        given(memberRepository.existsByEmail("hong@example.com")).willReturn(false);
        given(memberRepository.save(any(Member.class))).willReturn(savedMember);

        //When
        MemberResponse response =  memberService.createMember(request);

        //Then
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("홍길동");
        assertThat(response.getEmail()).isEqualTo("hong@example.com");
        assertThat(response.getMembershipType()).isEqualTo(MembershipType.REGULAR);

        // 이벤트 발행 확인
        ArgumentCaptor<MemberRegisteredEvent> eventCaptor = ArgumentCaptor.forClass(MemberRegisteredEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
    }

    @Test
    @DisplayName("회원 생성 - 이메일 중복 시 예외 발생")
    void createMember_이메일중복_예외발생() {
        //Given
        CreateMemberRequest request = CreateMemberRequest.builder()
                .name("홍길동")
                .email("hong@example.com")
                .build();

        given(memberRepository.existsByEmail("hong@example.com")).willReturn(true);

        //When & Then
        assertThatThrownBy(()->memberService.createMember(request))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessageContaining("hong@example.com");
    }

    @Test
    @DisplayName("회원 정보 조회 - 성공")
    void findMemberById_성공() {
        //Given
        Long memberId = 1L;
        Member member = Member.builder()
                .id(memberId)
                .name("홍길동")
                .email("hong@example.com")
                .membershipType(MembershipType.REGULAR)
                .build();

        given(memberRepository.findById(memberId)).willReturn(java.util.Optional.of(member));

        //When
        MemberResponse response = memberService.findMemberById(memberId);

        //Then
        assertThat(response.getId()).isEqualTo(memberId);
        assertThat(response.getName()).isEqualTo("홍길동");
        assertThat(response.getEmail()).isEqualTo("hong@example.com");
    }

    @Test
    @DisplayName("회원 정보 조회 - 캐시 미적용 시")
    void findMemberById_캐시미적용() {
        //Given
        Long memberId = 1L;
        Member member = Member.builder()
                .id(memberId)
                .name("홍길동")
                .email("hong@example.com")
                .membershipType(MembershipType.REGULAR)
                .build();
        given(memberRepository.findById(memberId)).willReturn(java.util.Optional.of(member));

        //When
        MemberResponse response = memberService.findMemberByIdWithoutCache(memberId);

        //Then
        assertThat(response.getId()).isEqualTo(memberId);
        assertThat(response.getName()).isEqualTo("홍길동");
    }

    @Test
    @DisplayName("회원 정보 조회 - 캐싱 성공")
    void findMemberById_캐싱_성공() {
        //Given
        Long memberId = 1L;
        Member member = Member.builder()
                .id(memberId)
                .name("홍길동")
                .email("hong@example.com")
                .build();
        given(memberRepository.findById(memberId)).willReturn(java.util.Optional.of(member));
        //When
        MemberResponse response1 = memberService.findMemberById(memberId);

        //Then
        assertThat(response1.getId()).isEqualTo(memberId);
        assertThat(response1.getName()).isEqualTo("홍길동");
    }

    @Test
    @DisplayName("회원 정보 수정 - 성공")
    void updateMember_success() {
        //Given
        Long memberId = 1L;
        Member member = Member.builder()
                .id(memberId)
                .name("홍길동")
                .email("hong@example.com")
                .build();

        given(memberRepository.save(any(Member.class))).willReturn(member);
        given(memberRepository.findById(memberId)).willReturn(java.util.Optional.of(member));

        UpdateMemberRequest request = UpdateMemberRequest.builder()
                .name("김철수")
                .email("kim@example.com")
                .build();
        //When
        MemberResponse response = memberService.updateMember(memberId, request);

        //Then
        assertThat(response.getId()).isEqualTo(memberId);
        assertThat(response.getName()).isEqualTo(request.getName());
        assertThat(response.getEmail()).isEqualTo(request.getEmail());
    }

    @Test
    @DisplayName("회원 목록 조회 - 성공")
    void findAllMembers_success() {
        //Given
        Member m1 = Member.builder().id(1L).name("A").email("a@example.com").membershipType(MembershipType.REGULAR).build();
        Member m2 = Member.builder().id(2L).name("B").email("b@example.com").membershipType(MembershipType.PREMIUM).build();

        given(memberRepository.findAll()).willReturn(List.of(m1, m2));
        //When
        List<MemberResponse> allMembers = memberService.findAllMembers();

        //Then
        assertThat(allMembers).hasSize(2);
        assertThat(allMembers.get(0).getId()).isEqualTo(1L);
        assertThat(allMembers.get(1).getMembershipType()).isEqualTo(MembershipType.PREMIUM);
    }

    @Test
    @DisplayName("전체 회원 목록 페이지 조회- 성공")
    void findAllMembersPage_success() {
        // Given
        Member m1 = Member.builder().id(1L).name("A").email("a@example.com").membershipType(MembershipType.REGULAR).build();
        Member m2 = Member.builder().id(2L).name("B").email("b@example.com").membershipType(MembershipType.PREMIUM).build();
        given(memberRepository.findAll()).willReturn(List.of(m1, m2));

        // When
        List<MemberResponse> list = memberService.findAllMembersPage(PageRequest.of(0, 10));

        // Then
        assertThat(list).hasSize(2);
        assertThat(list.get(0).getId()).isEqualTo(1L);
        assertThat(list.get(1).getMembershipType()).isEqualTo(MembershipType.PREMIUM);
    }

    @Test
    @DisplayName("이름으로 회원 조회 - 성공")
    void findMembersByName_success() {
        //Given
        String keyword = "kim";
        Member m1 = Member.builder().id(3L).name("Kim One").email("k1@example.com").membershipType(MembershipType.REGULAR).build();
        Member m2 = Member.builder().id(4L).name("Park").email("p@example.com").membershipType(MembershipType.REGULAR).build();

        given(memberRepository.findByNameContaining(keyword)).willReturn(List.of(m1));

        //When
        List<MemberResponse> list =  memberService.findMembersByName(keyword);

        //Then
        assertThat(list).hasSize(1);
        assertThat(list.get(0).getName()).containsIgnoringCase("kim");
        assertThat(list.get(0).getId()).isEqualTo(3L);
    }

    @Test
    @DisplayName("멤버쉬 타입으로 회원 조회 - 성공")
    void findMembersByMembershipType_success() {
        //Given
        Member m = Member.builder().id(5L).name("Prime").email("prime@example.com").membershipType(MembershipType.PREMIUM).build();
        given(memberRepository.findByMembershipType(MembershipType.PREMIUM)).willReturn(List.of(m));

        //When
        List<MemberResponse> list = memberService.findMembersByMembershipType(MembershipType.PREMIUM);

        //Then
        assertThat(list).hasSize(1);
        assertThat(list.get(0).getMembershipType()).isEqualTo(MembershipType.PREMIUM);
        assertThat(list.get(0).getEmail()).isEqualTo("prime@example.com");
    }
}
