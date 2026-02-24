package com.example.spring.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 배송 정보 요청 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryRequest {

    @NotBlank(message = "{validation.delivery.recipient.required}")
    private String recipientName;

    @NotBlank(message = "{validation.delivery.phone.required}")
    @Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$",
            message = "{validation.delivery.phone.format}")
    private String phoneNumber;

    private String zipCode;

    @NotBlank(message = "{validation.delivery.address.required}")
    private String address;

    private String addressDetail;

    private String deliveryMemo;  // 배송 메모
}