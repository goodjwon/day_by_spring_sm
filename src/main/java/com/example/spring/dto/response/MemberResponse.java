package com.example.spring.dto.response;

import com.example.spring.entity.Member;
import com.example.spring.entity.MembershipType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberResponse {
    private Long id;
    private String name;
    private String email;
    private MembershipType membershipType;
    private LocalDateTime joinDate;

    /**
     * Entity를 Response DTO로 변환
     */
    public static MemberResponse from(Member member) {
        return MemberResponse.builder()
                .id(member.getId())
                .name(member.getName())
                .email(member.getEmail())
                .membershipType(member.getMembershipType())
                .joinDate(member.getJoinDate())
                .build();
    }
}
