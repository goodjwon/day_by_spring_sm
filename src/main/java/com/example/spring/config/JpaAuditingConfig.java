package com.example.spring.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA Auditing 설정
 * - @CreatedDate, @LastModifiedDate 자동 관리
 * - @WebMvcTest와의 충돌 방지를 위해 별도 Configuration으로 분리
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}