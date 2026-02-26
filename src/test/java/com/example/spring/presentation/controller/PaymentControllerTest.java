package com.example.spring.presentation.controller;

import com.example.spring.application.dto.response.PaymentResponse;
import com.example.spring.domain.model.PaymentMethod;
import com.example.spring.domain.model.PaymentStatus;
import com.example.spring.exception.PaymentException;
import com.example.spring.application.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("PaymentController 통합 테스트")
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PaymentService paymentService;

    @Autowired
    private ObjectMapper objectMapper;

    private PaymentResponse pendingPaymentResponse;
    private PaymentResponse completedPaymentResponse;

    @BeforeEach
    void setUp() {
        // 테스트용 결제 대기 응답
        pendingPaymentResponse = PaymentResponse.builder()
                .id(1L)
                .orderId(100L)
                .method(PaymentMethod.CREDIT_CARD)
                .status(PaymentStatus.PENDING)
                .amount(new BigDecimal("15000"))
                .createdDate(LocalDateTime.now())
                .updatedDate(LocalDateTime.now())
                .build();

        // 테스트용 결제 완료 응답
        completedPaymentResponse = PaymentResponse.builder()
                .id(1L)
                .orderId(100L)
                .method(PaymentMethod.CREDIT_CARD)
                .status(PaymentStatus.COMPLETED)
                .amount(new BigDecimal("15000"))
                .transactionId("tx_20240106_001")
                .paymentDate(LocalDateTime.now())
                .createdDate(LocalDateTime.now())
                .updatedDate(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("결제 조회")
    class GetPaymentTest {

        @Test
        @DisplayName("결제 ID로 조회 성공")
        void getPaymentById_성공() throws Exception {
            // Given
            given(paymentService.findById(1L)).willReturn(pendingPaymentResponse);

            // When & Then
            mockMvc.perform(get("/api/payments/1"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.status").value("PENDING"))
                    .andExpect(jsonPath("$.amount").value(15000));

            verify(paymentService).findById(1L);
        }

        @Test
        @DisplayName("존재하지 않는 결제 ID로 조회 시 404")
        void getPaymentById_실패_404() throws Exception {
            // Given
            given(paymentService.findById(999L))
                    .willThrow(new PaymentException.PaymentNotFoundException(999L));

            // When & Then
            mockMvc.perform(get("/api/payments/999"))
                    .andDo(print())
                    .andExpect(status().isNotFound());

            verify(paymentService).findById(999L);
        }

        @Test
        @DisplayName("주문 ID로 결제 조회 성공")
        void getPaymentByOrderId_성공() throws Exception {
            // Given
            given(paymentService.findByOrderId(100L)).willReturn(pendingPaymentResponse);

            // When & Then
            mockMvc.perform(get("/api/payments/order/100"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.orderId").value(100L));

            verify(paymentService).findByOrderId(100L);
        }

        @Test
        @DisplayName("상태별 결제 조회 성공")
        void getPaymentsByStatus_성공() throws Exception {
            // Given
            given(paymentService.findByStatus(PaymentStatus.PENDING))
                    .willReturn(List.of(pendingPaymentResponse));

            // When & Then
            mockMvc.perform(get("/api/payments/status/PENDING"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].status").value("PENDING"));

            verify(paymentService).findByStatus(PaymentStatus.PENDING);
        }
    }

    @Nested
    @DisplayName("결제 상태 변경")
    class UpdatePaymentStatusTest {

        @Test
        @DisplayName("결제 완료 처리 성공")
        void completePayment_성공() throws Exception {
            // Given
            String transactionId = "tx_20240106_001";
            given(paymentService.completePayment(1L, transactionId))
                    .willReturn(completedPaymentResponse);

            // When & Then
            mockMvc.perform(patch("/api/payments/1/complete")
                            .param("transactionId", transactionId))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("COMPLETED"))
                    .andExpect(jsonPath("$.transactionId").value(transactionId));

            verify(paymentService).completePayment(1L, transactionId);
        }

        @Test
        @DisplayName("결제 실패 처리 성공")
        void failPayment_성공() throws Exception {
            // Given
            String reason = "잔액 부족";
            PaymentResponse failedPayment = PaymentResponse.builder()
                    .id(1L)
                    .status(PaymentStatus.FAILED)
                    .failureReason(reason)
                    .build();

            given(paymentService.failPayment(1L, reason)).willReturn(failedPayment);

            // When & Then
            mockMvc.perform(patch("/api/payments/1/fail")
                            .param("reason", reason))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("FAILED"))
                    .andExpect(jsonPath("$.failureReason").value(reason));

            verify(paymentService).failPayment(1L, reason);
        }

        @Test
        @DisplayName("결제 취소 성공")
        void cancelPayment_성공() throws Exception {
            // Given
            PaymentResponse cancelledPayment = PaymentResponse.builder()
                    .id(1L)
                    .status(PaymentStatus.CANCELLED)
                    .build();

            given(paymentService.cancelPayment(1L)).willReturn(cancelledPayment);

            // When & Then
            mockMvc.perform(patch("/api/payments/1/cancel"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("CANCELLED"));

            verify(paymentService).cancelPayment(1L);
        }

        @Test
        @DisplayName("결제 환불 성공")
        void refundPayment_성공() throws Exception {
            // Given
            BigDecimal refundAmount = new BigDecimal("15000");
            PaymentResponse refundedPayment = PaymentResponse.builder()
                    .id(1L)
                    .status(PaymentStatus.REFUNDED)
                    .refundedAmount(refundAmount)
                    .build();

            given(paymentService.refundPayment(eq(1L), eq(refundAmount))).willReturn(refundedPayment);

            // When & Then
            mockMvc.perform(patch("/api/payments/1/refund")
                            .param("refundAmount", "15000"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("REFUNDED"))
                    .andExpect(jsonPath("$.refundedAmount").value(15000));

            verify(paymentService).refundPayment(eq(1L), eq(refundAmount));
        }

        @Test
        @DisplayName("환불 실패 - 상태 오류시 400 반환")
        void refundPayment_실패_상태오류() throws Exception {
            // Given
            BigDecimal refundAmount = new BigDecimal("15000");
            given(paymentService.refundPayment(eq(1L), eq(refundAmount)))
                    .willThrow(new PaymentException.InvalidPaymentStateException("완료된 결제만 환불할 수 있습니다."));

            // When & Then
            mockMvc.perform(patch("/api/payments/1/refund")
                            .param("refundAmount", "15000"))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }
}