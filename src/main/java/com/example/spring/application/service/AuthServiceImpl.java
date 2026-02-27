package com.example.spring.application.service;

import com.example.spring.config.SecurityConfig;
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
import com.example.spring.application.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    @Override
    @Transactional
    public MemberResponse signup(SignupRequest request) {
        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new MemberException.DuplicateEmailException(request.getEmail());
        }

        // 최초 가입자 1명만 ADMIN, 이후 가입자는 USER 고정
        Role assignedRole = memberRepository.count() == 0 ? Role.ADMIN : Role.USER;

        Member member = Member.builder()
                .email(request.getEmail())
                .name(request.getName())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(assignedRole)
                .membershipType(MembershipType.REGULAR)
                .joinDate(LocalDateTime.now())
                .build();

        Member savedMember = memberRepository.save(member);
        return MemberResponse.from(savedMember);
    }

    @Override
    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {
        // 1. Login ID/PW를 기반으로 Authentication 객체 생성
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword());

        // 2. 실제 검증 (사용자 비밀번호 체크)이 이루어지는 부분
        // authenticate 메서드가 실행될 때 CustomUserDetailsService의 loadUserByUsername 메서드가 실행됨
        Authentication authentication = authenticationManager.authenticate(authenticationToken);

        // 3. 인증 정보를 기반으로 JWT 토큰 생성
        String jwt = jwtTokenProvider.createToken(authentication);

        return TokenResponse.builder()
                .accessToken(jwt)
                .tokenType("Bearer")
                .expiresIn(3600000L) // 1시간 (Provider 설정과 맞춰야 함)
                .build();
    }
}