package com.example.spring.application;

import com.example.spring.application.dto.request.LoginRequest;
import com.example.spring.application.dto.request.SignupRequest;
import com.example.spring.application.dto.response.MemberResponse;
import com.example.spring.application.dto.response.TokenResponse;

public interface AuthService {
    MemberResponse signup(SignupRequest request);
    TokenResponse login(LoginRequest request);
}