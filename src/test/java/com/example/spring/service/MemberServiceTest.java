package com.example.spring.service;

import com.example.spring.dto.request.CreateMemberRequest;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
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
}
