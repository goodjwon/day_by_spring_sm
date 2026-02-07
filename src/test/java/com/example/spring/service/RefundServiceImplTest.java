package com.example.spring.service;

import com.example.spring.domain.vo.Money;
import com.example.spring.dto.request.RefundRequest;
import com.example.spring.dto.response.RefundResponse;
import com.example.spring.entity.*;
import com.example.spring.exception.OrderException;
import com.example.spring.exception.RefundException;
import com.example.spring.repository.OrderRepository;
import com.example.spring.repository.RefundRepository;
import com.example.spring.service.impl.RefundServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("RefundServiceImpl 테스트")
class RefundServiceImplTest {

    @Mock
    private RefundRepository refundRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private RefundServiceImpl refundService;

    private Member testMember;
    private Order testOrder;
    private Refund testRefund;
    private RefundRequest testRefundRequest;

    @BeforeEach
    void setUp() {
        testMember = Member.builder()
                .id(1L)
                .name("테스트 회원")
                .email("test@test.com")
                .password("password")
                .role(Role.USER)
                .membershipType(MembershipType.REGULAR)
                .build();

        testOrder = Order.builder()
                .id(1L)
                .member(testMember)
                .totalAmount(Money.of(new BigDecimal("50000")))
                .orderDate(LocalDateTime.now())
                .status(OrderStatus.DELIVERED)
                .build();

        testRefund = Refund.builder()
                .id(1L)
                .order(testOrder)
                .status(RefundStatus.REQUESTED)
                .amount(Money.of(new BigDecimal("30000")))
                .reason("상품 불량")
                .requestedBy("고객")
                .bankName("국민은행")
                .accountNumber("123-456-789")
                .accountHolder("홍길동")
                .requestDate(LocalDateTime.now())
                .build();

        testRefundRequest = RefundRequest.builder()
                .orderId(1L)
                .amount(new BigDecimal("30000"))
                .reason("상품 불량")
                .bankName("국민은행")
                .accountNumber("123-456-789")
                .accountHolder("홍길동")
                .requestedBy("고객")
                .build();
    }

    // ========== createRefund 테스트 ==========

    @Nested
    @DisplayName("createRefund 테스트")
    class CreateRefundTest {

        @Test
        @DisplayName("환불 요청 생성 성공")
        void createRefund_정상요청_생성성공() {
            // Given
            given(orderRepository.findById(1L)).willReturn(Optional.of(testOrder));
            given(refundRepository.save(any(Refund.class))).willAnswer(invocation -> {
                Refund refund = invocation.getArgument(0);
                ReflectionTestUtils.setField(refund, "id", 1L);
                return refund;
            });

            // When
            RefundResponse response = refundService.createRefund(testRefundRequest);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getAmount()).isEqualByComparingTo(new BigDecimal("30000"));
            assertThat(response.getReason()).isEqualTo("상품 불량");
            assertThat(response.getStatus()).isEqualTo(RefundStatus.REQUESTED);
            verify(refundRepository).save(any(Refund.class));
        }

        @Test
        @DisplayName("존재하지 않는 주문에 대한 환불 요청 시 예외")
        void createRefund_존재하지않는주문_예외() {
            // Given
            given(orderRepository.findById(999L)).willReturn(Optional.empty());
            testRefundRequest.setOrderId(999L);

            // When & Then
            assertThatThrownBy(() -> refundService.createRefund(testRefundRequest))
                    .isInstanceOf(OrderException.OrderNotFoundException.class);
        }
    }

    // ========== findById 테스트 ==========

    @Nested
    @DisplayName("findById 테스트")
    class FindByIdTest {

        @Test
        @DisplayName("환불 ID로 조회 성공")
        void findById_존재하는환불_성공() {
            // Given
            given(refundRepository.findById(1L)).willReturn(Optional.of(testRefund));

            // When
            RefundResponse response = refundService.findById(1L);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getAmount()).isEqualByComparingTo(new BigDecimal("30000"));
        }

        @Test
        @DisplayName("존재하지 않는 환불 ID로 조회 시 예외")
        void findById_존재하지않는환불_예외() {
            // Given
            given(refundRepository.findById(999L)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> refundService.findById(999L))
                    .isInstanceOf(RefundException.RefundNotFoundException.class);
        }
    }

    // ========== findByOrderId 테스트 ==========

    @Nested
    @DisplayName("findByOrderId 테스트")
    class FindByOrderIdTest {

        @Test
        @DisplayName("주문 ID로 환불 목록 조회 성공")
        void findByOrderId_환불목록_조회성공() {
            // Given
            Refund refund2 = Refund.builder()
                    .id(2L)
                    .order(testOrder)
                    .status(RefundStatus.COMPLETED)
                    .amount(Money.of(new BigDecimal("10000")))
                    .reason("단순 변심")
                    .requestDate(LocalDateTime.now())
                    .build();

            given(refundRepository.findByOrderId(1L)).willReturn(Arrays.asList(testRefund, refund2));

            // When
            List<RefundResponse> responses = refundService.findByOrderId(1L);

            // Then
            assertThat(responses).hasSize(2);
        }

        @Test
        @DisplayName("환불 없는 주문 조회 시 빈 리스트")
        void findByOrderId_환불없음_빈리스트() {
            // Given
            given(refundRepository.findByOrderId(999L)).willReturn(Collections.emptyList());

            // When
            List<RefundResponse> responses = refundService.findByOrderId(999L);

            // Then
            assertThat(responses).isEmpty();
        }
    }

    // ========== findByStatus 테스트 ==========

    @Nested
    @DisplayName("findByStatus 테스트")
    class FindByStatusTest {

        @Test
        @DisplayName("상태별 환불 목록 조회 성공")
        void findByStatus_환불목록_조회성공() {
            // Given
            given(refundRepository.findByStatus(RefundStatus.REQUESTED))
                    .willReturn(Collections.singletonList(testRefund));

            // When
            List<RefundResponse> responses = refundService.findByStatus(RefundStatus.REQUESTED);

            // Then
            assertThat(responses).hasSize(1);
            assertThat(responses.get(0).getStatus()).isEqualTo(RefundStatus.REQUESTED);
        }

        @Test
        @DisplayName("해당 상태 환불 없으면 빈 리스트")
        void findByStatus_없음_빈리스트() {
            // Given
            given(refundRepository.findByStatus(RefundStatus.REJECTED))
                    .willReturn(Collections.emptyList());

            // When
            List<RefundResponse> responses = refundService.findByStatus(RefundStatus.REJECTED);

            // Then
            assertThat(responses).isEmpty();
        }
    }

    // ========== findPendingRefunds 테스트 ==========

    @Nested
    @DisplayName("findPendingRefunds 테스트")
    class FindPendingRefundsTest {

        @Test
        @DisplayName("대기 중인 환불 목록 조회 성공")
        void findPendingRefunds_대기중환불_조회성공() {
            // Given
            given(refundRepository.findPendingRefunds())
                    .willReturn(Collections.singletonList(testRefund));

            // When
            List<RefundResponse> responses = refundService.findPendingRefunds();

            // Then
            assertThat(responses).hasSize(1);
            assertThat(responses.get(0).getStatus()).isEqualTo(RefundStatus.REQUESTED);
        }

        @Test
        @DisplayName("대기 중인 환불 없으면 빈 리스트")
        void findPendingRefunds_없음_빈리스트() {
            // Given
            given(refundRepository.findPendingRefunds()).willReturn(Collections.emptyList());

            // When
            List<RefundResponse> responses = refundService.findPendingRefunds();

            // Then
            assertThat(responses).isEmpty();
        }
    }

    // ========== approveRefund 테스트 ==========

    @Nested
    @DisplayName("approveRefund 테스트")
    class ApproveRefundTest {

        @Test
        @DisplayName("환불 승인 성공")
        void approveRefund_요청상태_승인성공() {
            // Given
            given(refundRepository.findById(1L)).willReturn(Optional.of(testRefund));
            given(refundRepository.save(any(Refund.class))).willAnswer(invocation -> invocation.getArgument(0));

            // When
            RefundResponse response = refundService.approveRefund(1L, "관리자");

            // Then
            assertThat(response.getStatus()).isEqualTo(RefundStatus.APPROVED);
            assertThat(response.getApprovedBy()).isEqualTo("관리자");
            verify(refundRepository).save(any(Refund.class));
        }

        @Test
        @DisplayName("이미 승인된 환불 재승인 시 예외")
        void approveRefund_승인상태_예외() {
            // Given
            ReflectionTestUtils.setField(testRefund, "status", RefundStatus.APPROVED);
            given(refundRepository.findById(1L)).willReturn(Optional.of(testRefund));

            // When & Then
            assertThatThrownBy(() -> refundService.approveRefund(1L, "관리자"))
                    .isInstanceOf(RefundException.InvalidRefundStateException.class)
                    .hasMessageContaining("요청된 환불만 승인");
        }

        @Test
        @DisplayName("존재하지 않는 환불 승인 시 예외")
        void approveRefund_존재하지않는환불_예외() {
            // Given
            given(refundRepository.findById(999L)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> refundService.approveRefund(999L, "관리자"))
                    .isInstanceOf(RefundException.RefundNotFoundException.class);
        }
    }

    // ========== rejectRefund 테스트 ==========

    @Nested
    @DisplayName("rejectRefund 테스트")
    class RejectRefundTest {

        @Test
        @DisplayName("환불 거부 성공")
        void rejectRefund_요청상태_거부성공() {
            // Given
            given(refundRepository.findById(1L)).willReturn(Optional.of(testRefund));
            given(refundRepository.save(any(Refund.class))).willAnswer(invocation -> invocation.getArgument(0));

            // When
            RefundResponse response = refundService.rejectRefund(1L, "관리자", "환불 조건 미충족");

            // Then
            assertThat(response.getStatus()).isEqualTo(RefundStatus.REJECTED);
            assertThat(response.getRejectedBy()).isEqualTo("관리자");
            assertThat(response.getRejectionReason()).isEqualTo("환불 조건 미충족");
            verify(refundRepository).save(any(Refund.class));
        }

        @Test
        @DisplayName("이미 승인된 환불 거부 시 예외")
        void rejectRefund_승인상태_예외() {
            // Given
            ReflectionTestUtils.setField(testRefund, "status", RefundStatus.APPROVED);
            given(refundRepository.findById(1L)).willReturn(Optional.of(testRefund));

            // When & Then
            assertThatThrownBy(() -> refundService.rejectRefund(1L, "관리자", "사유"))
                    .isInstanceOf(RefundException.InvalidRefundStateException.class)
                    .hasMessageContaining("요청된 환불만 거부");
        }

        @Test
        @DisplayName("존재하지 않는 환불 거부 시 예외")
        void rejectRefund_존재하지않는환불_예외() {
            // Given
            given(refundRepository.findById(999L)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> refundService.rejectRefund(999L, "관리자", "사유"))
                    .isInstanceOf(RefundException.RefundNotFoundException.class);
        }
    }

    // ========== startProcessing 테스트 ==========

    @Nested
    @DisplayName("startProcessing 테스트")
    class StartProcessingTest {

        @Test
        @DisplayName("환불 처리 시작 성공")
        void startProcessing_승인상태_처리시작성공() {
            // Given
            ReflectionTestUtils.setField(testRefund, "status", RefundStatus.APPROVED);
            given(refundRepository.findById(1L)).willReturn(Optional.of(testRefund));
            given(refundRepository.save(any(Refund.class))).willAnswer(invocation -> invocation.getArgument(0));

            // When
            RefundResponse response = refundService.startProcessing(1L);

            // Then
            assertThat(response.getStatus()).isEqualTo(RefundStatus.PROCESSING);
            verify(refundRepository).save(any(Refund.class));
        }

        @Test
        @DisplayName("요청 상태에서 처리 시작 시 예외")
        void startProcessing_요청상태_예외() {
            // Given
            // testRefund is already in REQUESTED status from setUp()
            given(refundRepository.findById(1L)).willReturn(Optional.of(testRefund));

            // When & Then
            assertThatThrownBy(() -> refundService.startProcessing(1L))
                    .isInstanceOf(RefundException.InvalidRefundStateException.class)
                    .hasMessageContaining("승인된 환불만 처리를 시작");
        }

        @Test
        @DisplayName("존재하지 않는 환불 처리 시작 시 예외")
        void startProcessing_존재하지않는환불_예외() {
            // Given
            given(refundRepository.findById(999L)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> refundService.startProcessing(999L))
                    .isInstanceOf(RefundException.RefundNotFoundException.class);
        }
    }

    // ========== completeRefund 테스트 ==========

    @Nested
    @DisplayName("completeRefund 테스트")
    class CompleteRefundTest {

        @Test
        @DisplayName("환불 완료 성공")
        void completeRefund_처리중상태_완료성공() {
            // Given
            ReflectionTestUtils.setField(testRefund, "status", RefundStatus.PROCESSING);
            given(refundRepository.findById(1L)).willReturn(Optional.of(testRefund));
            given(refundRepository.save(any(Refund.class))).willAnswer(invocation -> invocation.getArgument(0));

            // When
            RefundResponse response = refundService.completeRefund(1L, "TXN-12345");

            // Then
            assertThat(response.getStatus()).isEqualTo(RefundStatus.COMPLETED);
            assertThat(response.getRefundTransactionId()).isEqualTo("TXN-12345");
            verify(refundRepository).save(any(Refund.class));
        }

        @Test
        @DisplayName("승인 상태에서 완료 시도 시 예외")
        void completeRefund_승인상태_예외() {
            // Given
            ReflectionTestUtils.setField(testRefund, "status", RefundStatus.APPROVED);
            given(refundRepository.findById(1L)).willReturn(Optional.of(testRefund));

            // When & Then
            assertThatThrownBy(() -> refundService.completeRefund(1L, "TXN-12345"))
                    .isInstanceOf(RefundException.InvalidRefundStateException.class)
                    .hasMessageContaining("처리중인 환불만 완료");
        }

        @Test
        @DisplayName("존재하지 않는 환불 완료 시 예외")
        void completeRefund_존재하지않는환불_예외() {
            // Given
            given(refundRepository.findById(999L)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> refundService.completeRefund(999L, "TXN-12345"))
                    .isInstanceOf(RefundException.RefundNotFoundException.class);
        }
    }

    // ========== failRefund 테스트 ==========

    @Nested
    @DisplayName("failRefund 테스트")
    class FailRefundTest {

        @Test
        @DisplayName("환불 실패 처리 성공")
        void failRefund_실패처리_성공() {
            // Given
            ReflectionTestUtils.setField(testRefund, "status", RefundStatus.PROCESSING);
            given(refundRepository.findById(1L)).willReturn(Optional.of(testRefund));
            given(refundRepository.save(any(Refund.class))).willAnswer(invocation -> invocation.getArgument(0));

            // When
            RefundResponse response = refundService.failRefund(1L, "계좌 정보 오류");

            // Then
            assertThat(response.getStatus()).isEqualTo(RefundStatus.FAILED);
            assertThat(response.getProcessingMemo()).isEqualTo("계좌 정보 오류");
            verify(refundRepository).save(any(Refund.class));
        }

        @Test
        @DisplayName("존재하지 않는 환불 실패 처리 시 예외")
        void failRefund_존재하지않는환불_예외() {
            // Given
            given(refundRepository.findById(999L)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> refundService.failRefund(999L, "사유"))
                    .isInstanceOf(RefundException.RefundNotFoundException.class);
        }
    }

    // ========== getTotalRefundedAmount 테스트 ==========

    @Nested
    @DisplayName("getTotalRefundedAmount 테스트")
    class GetTotalRefundedAmountTest {

        @Test
        @DisplayName("총 환불 금액 조회 성공")
        void getTotalRefundedAmount_조회성공() {
            // Given
            given(refundRepository.calculateTotalRefundedAmount(1L))
                    .willReturn(new BigDecimal("40000"));

            // When
            BigDecimal totalAmount = refundService.getTotalRefundedAmount(1L);

            // Then
            assertThat(totalAmount).isEqualByComparingTo(new BigDecimal("40000"));
        }

        @Test
        @DisplayName("환불 없는 주문의 총 환불 금액은 0")
        void getTotalRefundedAmount_환불없음_0() {
            // Given
            given(refundRepository.calculateTotalRefundedAmount(999L))
                    .willReturn(BigDecimal.ZERO);

            // When
            BigDecimal totalAmount = refundService.getTotalRefundedAmount(999L);

            // Then
            assertThat(totalAmount).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }
}