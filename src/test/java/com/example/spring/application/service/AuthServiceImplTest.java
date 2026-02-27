package com.example.spring.application.service;

import com.example.spring.application.dto.request.LoginRequest;
import com.example.spring.application.dto.request.SignupRequest;
import com.example.spring.application.dto.response.MemberResponse;
import com.example.spring.application.dto.response.TokenResponse;
import com.example.spring.domain.model.Member;
import com.example.spring.domain.model.MembershipType;
import com.example.spring.domain.model.Role;
import com.example.spring.exception.MemberException;
import com.example.spring.domain.repository.MemberRepository;
import com.example.spring.infrastructure.security.JwtTokenProvider;
import com.example.spring.application.service.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthServiceImpl 테스트")
class AuthServiceImplTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthServiceImpl authService;

    private SignupRequest signupRequest;
    private LoginRequest loginRequest;
    private Member savedMember;

    @BeforeEach
    void setUp() {
        signupRequest = new SignupRequest();
        ReflectionTestUtils.setField(signupRequest, "name", "홍길동");
        ReflectionTestUtils.setField(signupRequest, "email", "hong@test.com");
        ReflectionTestUtils.setField(signupRequest, "password", "password123");

        loginRequest = new LoginRequest();
        ReflectionTestUtils.setField(loginRequest, "email", "hong@test.com");
        ReflectionTestUtils.setField(loginRequest, "password", "password123");

        savedMember = Member.builder()
                .id(1L)
                .name("홍길동")
                .email("hong@test.com")
                .password("encodedPassword")
                .role(Role.USER)
                .membershipType(MembershipType.REGULAR)
                .build();
    }

    // ========== signup 테스트 ==========

    @Nested
    @DisplayName("signup 테스트")
    class SignupTest {

        @Test
        @DisplayName("최초 가입자는 ADMIN으로 가입")
        void signup_정상요청_가입성공() {
            // Given
            given(memberRepository.existsByEmail("hong@test.com")).willReturn(false);
            given(memberRepository.count()).willReturn(0L);
            given(passwordEncoder.encode("password123")).willReturn("encodedPassword");
            Member firstMember = Member.builder()
                    .id(1L)
                    .name("홍길동")
                    .email("hong@test.com")
                    .password("encodedPassword")
                    .role(Role.ADMIN)
                    .membershipType(MembershipType.REGULAR)
                    .build();
            given(memberRepository.save(any(Member.class))).willReturn(firstMember);

            // When
            MemberResponse response = authService.signup(signupRequest);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getName()).isEqualTo("홍길동");
            assertThat(response.getEmail()).isEqualTo("hong@test.com");
            assertThat(response.getRole()).isEqualTo(Role.ADMIN);
            verify(memberRepository).save(any(Member.class));
        }

        @Test
        @DisplayName("중복 이메일로 가입 시 예외")
        void signup_중복이메일_예외() {
            // Given
            given(memberRepository.existsByEmail("hong@test.com")).willReturn(true);

            // When & Then
            assertThatThrownBy(() -> authService.signup(signupRequest))
                    .isInstanceOf(MemberException.DuplicateEmailException.class);
            verify(memberRepository, never()).save(any(Member.class));
        }

        @Test
        @DisplayName("최초 가입자가 아니면 USER로 가입")
        void signup_notFirstMember_기본값USER() {
            // Given
            given(memberRepository.existsByEmail("hong@test.com")).willReturn(false);
            given(memberRepository.count()).willReturn(5L);
            given(passwordEncoder.encode("password123")).willReturn("encodedPassword");
            given(memberRepository.save(any(Member.class))).willAnswer(invocation -> {
                Member member = invocation.getArgument(0);
                ReflectionTestUtils.setField(member, "id", 1L);
                return member;
            });

            // When
            MemberResponse response = authService.signup(signupRequest);

            // Then
            verify(memberRepository).save(argThat(member -> member.getRole() == Role.USER));
        }
    }

    // ========== login 테스트 ==========

    @Nested
    @DisplayName("login 테스트")
    class LoginTest {

        @Test
        @DisplayName("로그인 성공")
        void login_정상인증_토큰반환() {
            // Given
            given(authenticationManager.authenticate(any())).willReturn(authentication);
            given(jwtTokenProvider.createToken(authentication)).willReturn("jwt-token-12345");

            // When
            TokenResponse response = authService.login(loginRequest);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getAccessToken()).isEqualTo("jwt-token-12345");
            assertThat(response.getTokenType()).isEqualTo("Bearer");
            assertThat(response.getExpiresIn()).isEqualTo(3600000L);
        }

        @Test
        @DisplayName("잘못된 비밀번호로 로그인 시 인증 실패")
        void login_잘못된비밀번호_예외() {
            // Given
            given(authenticationManager.authenticate(any()))
                    .willThrow(new org.springframework.security.authentication.BadCredentialsException("Bad credentials"));

            // When & Then
            assertThatThrownBy(() -> authService.login(loginRequest))
                    .isInstanceOf(org.springframework.security.authentication.BadCredentialsException.class);
        }

        @Test
        @DisplayName("존재하지 않는 이메일로 로그인 시 인증 실패")
        void login_존재하지않는이메일_예외() {
            // Given
            ReflectionTestUtils.setField(loginRequest, "email", "nonexistent@test.com");
            given(authenticationManager.authenticate(any()))
                    .willThrow(new org.springframework.security.authentication.BadCredentialsException("Bad credentials"));

            // When & Then
            assertThatThrownBy(() -> authService.login(loginRequest))
                    .isInstanceOf(org.springframework.security.authentication.BadCredentialsException.class);
        }
    }
}