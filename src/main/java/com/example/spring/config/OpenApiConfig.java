package com.example.spring.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI (Swagger) 설정 클래스
 * API 문서의 메타데이터를 정의합니다.
 */
@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                // JWT 보안 스킴 추가
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT 토큰을 입력하세요 (Bearer 접두사는 자동으로 추가됩니다)")))
                // 전역 보안 요구사항 추가 (모든 API에 적용)
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .info(new Info()
                        .title("도서 관리 시스템 API")
                        .description("""
                                Spring Boot 기반 도서 관리 시스템 REST API 명세서

                                ## 📚 주요 기능

                                ### 대여 관리 (Loan Management)
                                - **관리자 API** (`/api/admin/loans`)
                                  - 전체 대출 목록 조회 (페이징, 검색, 필터링, 정렬)
                                  - 대출 생성, 조회, 수정, 삭제
                                  - 연체 관리 및 통계
                                - **사용자 API** (`/api/client/loans`)
                                  - 내 대출 목록 조회
                                  - 도서 반납 신청
                                  - 대출 이력 삭제
                                - **추가 기능**
                                  - JOIN 쿼리를 활용한 고급 검색
                                  - N+1 문제 해결 (Fetch Join)
                                  - 회원/도서별 대출 내역

                                ### 도서 관리 (Book Management)
                                - 도서 CRUD (생성, 조회, 수정, 삭제)
                                - 검색 기능 (제목, 저자, ISBN, 키워드)
                                - 재고 관리 및 통계
                                - Soft Delete 지원

                                ### 회원 관리 (Member Management)
                                - 회원 CRUD
                                - 회원십 업그레이드
                                - 대출 한도 조회
                                - 이메일 중복 확인

                                ### 주문 관리 (Order Management)
                                - 주문 생성 및 조회
                                - 주문 상태 관리
                                - 주문 항목 관리

                                ## 📖 API 문서
                                - **Loan API 상세 가이드**: LOAN_GUIDE.md 참고
                                - **전체 API 가이드**: API_GUIDE.md 참고

                                ## 🔧 기술 스택
                                - Spring Boot 3.5.3
                                - Spring Data JPA (Hibernate)
                                - Spring AOP (로깅)
                                - H2 Database (개발), MySQL (운영)
                                - Springdoc OpenAPI 3.0

                                ## 🔐 인증 (JWT Bearer Token)

                                이 API는 JWT(JSON Web Token) 기반 인증을 사용합니다.

                                ### 인증이 필요 없는 엔드포인트
                                - `/api/auth/**` - 회원가입, 로그인
                                - `/swagger-ui/**`, `/v3/api-docs/**` - API 문서
                                - `/h2-console/**` - H2 데이터베이스 콘솔

                                ### 인증이 필요한 엔드포인트
                                - `/api/admin/**` - **ADMIN** 역할 필요
                                - `/api/client/**` - **USER** 또는 **ADMIN** 역할 필요
                                - 기타 모든 API - 인증 필요

                                ### 사용 방법
                                1. `/api/auth/signup`로 회원가입
                                2. `/api/auth/login`으로 로그인하여 JWT 토큰 획득
                                3. Swagger UI 우측 상단 **Authorize** 버튼 클릭
                                4. `Bearer {토큰}` 형식으로 입력 (또는 토큰만 입력)
                                5. 인증이 필요한 API 호출

                                ## 📊 응답 코드
                                - **200 OK**: 성공적인 조회/수정
                                - **201 Created**: 성공적인 생성
                                - **204 No Content**: 성공적인 삭제
                                - **400 Bad Request**: 잘못된 요청 데이터
                                - **404 Not Found**: 리소스를 찾을 수 없음
                                - **409 Conflict**: 비즈니스 규칙 위반 (중복, 한도 초과 등)
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("개발팀")
                                .email("dev@example.com")
                                .url("https://github.com/example/library-system"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("개발 서버 (H2 Database)"),
                        new Server()
                                .url("https://api.example.com")
                                .description("운영 서버 (MySQL)")
                ));
    }
}