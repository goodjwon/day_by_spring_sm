package com.example.spring.dto.request;

import com.example.spring.entity.MembershipType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateMemberRequest {
    @NotBlank(message = "이름은 필수 입니다")
    @Size(min = 2, max = 50, message = "이름은 2자 이상 50자 이하 입니다")
    private String name;

    @Email(message = "올바른 이메일 형식이 아닙니다")
    @NotBlank(message = "이메일은 필수입니다")
    private String email;

    // 멤버십 타입은 선택사항 (기본값: REGULAR)
    private MembershipType membershipType;

}
