package com.example.spring.application.dto.request;

import com.example.spring.domain.model.MembershipType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 회원 생성 요청 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateMemberRequest {

    @NotBlank(message = "{validation.name.required}")
    @Size(min = 2, max = 50, message = "{validation.name.size.2to50}")
    private String name;

    @Email(message = "{validation.email.format}")
    @NotBlank(message = "{validation.email.required}")
    private String email;

    // 멤버십 타입은 선택사항 (기본값: REGULAR)
    private MembershipType membershipType;
}