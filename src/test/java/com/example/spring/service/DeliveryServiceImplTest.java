package com.example.spring.service;

import com.example.spring.domain.vo.Address;
import com.example.spring.domain.vo.Money;
import com.example.spring.entity.Role;
import com.example.spring.dto.response.DeliveryResponse;
import com.example.spring.entity.*;
import com.example.spring.exception.DeliveryException;
import com.example.spring.repository.DeliveryRepository;
import com.example.spring.service.impl.DeliveryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
@DisplayName("DeliveryServiceImpl 테스트")
class DeliveryServiceImplTest {

    @Mock
    private DeliveryRepository deliveryRepository;

    @InjectMocks
    private DeliveryServiceImpl deliveryService;

    private Member testMember;
    private Order testOrder;
    private Delivery testDelivery;

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
                .status(OrderStatus.PENDING)
                .build();

        testDelivery = Delivery.builder()
                .id(1L)
                .order(testOrder)
                .recipientName("홍길동")
                .phoneNumber("010-1234-5678")
                .deliveryAddress(Address.of("06234", "서울시 강남구 테헤란로 123", ""))
                .status(DeliveryStatus.PREPARING)
                .build();
    }

    // ========== findById 테스트 ==========

    @Nested
    @DisplayName("findById 테스트")
    class FindByIdTest {

        @Test
        @DisplayName("존재하는 배송 ID로 조회 성공")
        void findById_존재하는배송_성공() {
            // Given
            given(deliveryRepository.findById(1L)).willReturn(Optional.of(testDelivery));

            // When
            DeliveryResponse response = deliveryService.findById(1L);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getRecipientName()).isEqualTo("홍길동");
        }

        @Test
        @DisplayName("존재하지 않는 배송 ID로 조회 시 예외 발생")
        void findById_존재하지않는배송_예외() {
            // Given
            given(deliveryRepository.findById(999L)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> deliveryService.findById(999L))
                    .isInstanceOf(DeliveryException.DeliveryNotFoundException.class);
        }
    }

    // ========== findByOrderId 테스트 ==========

    @Nested
    @DisplayName("findByOrderId 테스트")
    class FindByOrderIdTest {

        @Test
        @DisplayName("주문 ID로 배송 조회 성공")
        void findByOrderId_존재하는주문_성공() {
            // Given
            given(deliveryRepository.findByOrderId(1L)).willReturn(Optional.of(testDelivery));

            // When
            DeliveryResponse response = deliveryService.findByOrderId(1L);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getOrderId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("존재하지 않는 주문 ID로 조회 시 예외 발생")
        void findByOrderId_존재하지않는주문_예외() {
            // Given
            given(deliveryRepository.findByOrderId(999L)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> deliveryService.findByOrderId(999L))
                    .isInstanceOf(DeliveryException.DeliveryNotFoundException.class);
        }
    }

    // ========== findByTrackingNumber 테스트 ==========

    @Nested
    @DisplayName("findByTrackingNumber 테스트")
    class FindByTrackingNumberTest {

        @Test
        @DisplayName("운송장 번호로 배송 조회 성공")
        void findByTrackingNumber_존재하는운송장_성공() {
            // Given
            ReflectionTestUtils.setField(testDelivery, "trackingNumber", "123456789012");
            given(deliveryRepository.findByTrackingNumber("123456789012"))
                    .willReturn(Optional.of(testDelivery));

            // When
            DeliveryResponse response = deliveryService.findByTrackingNumber("123456789012");

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getTrackingNumber()).isEqualTo("123456789012");
        }

        @Test
        @DisplayName("존재하지 않는 운송장 번호로 조회 시 예외 발생")
        void findByTrackingNumber_존재하지않는운송장_예외() {
            // Given
            given(deliveryRepository.findByTrackingNumber("NON-EXISTENT"))
                    .willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> deliveryService.findByTrackingNumber("NON-EXISTENT"))
                    .isInstanceOf(DeliveryException.DeliveryNotFoundException.class);
        }
    }

    // ========== findByStatus 테스트 ==========

    @Nested
    @DisplayName("findByStatus 테스트")
    class FindByStatusTest {

        @Test
        @DisplayName("상태별 배송 목록 조회 성공")
        void findByStatus_배송목록_조회성공() {
            // Given
            Order order2 = Order.builder()
                    .id(2L)
                    .member(testMember)
                    .totalAmount(Money.of(new BigDecimal("30000")))
                    .status(OrderStatus.PENDING)
                    .build();
            Delivery delivery2 = Delivery.builder()
                    .id(2L)
                    .order(order2)
                    .recipientName("김영희")
                    .phoneNumber("010-9999-8888")
                    .deliveryAddress(Address.of("06234", "서울시 서초구", ""))
                    .status(DeliveryStatus.PREPARING)
                    .build();

            given(deliveryRepository.findByStatus(DeliveryStatus.PREPARING))
                    .willReturn(Arrays.asList(testDelivery, delivery2));

            // When
            List<DeliveryResponse> responses = deliveryService.findByStatus(DeliveryStatus.PREPARING);

            // Then
            assertThat(responses).hasSize(2);
        }

        @Test
        @DisplayName("해당 상태 배송 없으면 빈 리스트 반환")
        void findByStatus_없음_빈리스트() {
            // Given
            given(deliveryRepository.findByStatus(DeliveryStatus.DELIVERED))
                    .willReturn(Collections.emptyList());

            // When
            List<DeliveryResponse> responses = deliveryService.findByStatus(DeliveryStatus.DELIVERED);

            // Then
            assertThat(responses).isEmpty();
        }
    }

    // ========== startShipping 테스트 ==========

    @Nested
    @DisplayName("startShipping 테스트")
    class StartShippingTest {

        @Test
        @DisplayName("배송 시작 성공")
        void startShipping_준비중상태_배송시작성공() {
            // Given
            given(deliveryRepository.findById(1L)).willReturn(Optional.of(testDelivery));
            given(deliveryRepository.save(any(Delivery.class))).willAnswer(invocation -> invocation.getArgument(0));

            // When
            DeliveryResponse response = deliveryService.startShipping(1L, "123456789012", "CJ대한통운");

            // Then
            assertThat(response.getStatus()).isEqualTo(DeliveryStatus.IN_TRANSIT);
            assertThat(response.getTrackingNumber()).isEqualTo("123456789012");
            assertThat(response.getCourierCompany()).isEqualTo("CJ대한통운");
            verify(deliveryRepository).save(any(Delivery.class));
        }

        @Test
        @DisplayName("배송 준비중이 아닌 상태에서 배송 시작 시 예외")
        void startShipping_배송중상태_예외() {
            // Given
            ReflectionTestUtils.setField(testDelivery, "status", DeliveryStatus.IN_TRANSIT);
            given(deliveryRepository.findById(1L)).willReturn(Optional.of(testDelivery));

            // When & Then
            assertThatThrownBy(() -> deliveryService.startShipping(1L, "123456789012", "CJ대한통운"))
                    .isInstanceOf(DeliveryException.InvalidDeliveryStateException.class)
                    .hasMessageContaining("배송 준비중인 상태에서만");
        }

        @Test
        @DisplayName("존재하지 않는 배송 ID로 배송 시작 시 예외")
        void startShipping_존재하지않는배송_예외() {
            // Given
            given(deliveryRepository.findById(999L)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> deliveryService.startShipping(999L, "123456789012", "CJ대한통운"))
                    .isInstanceOf(DeliveryException.DeliveryNotFoundException.class);
        }
    }

    // ========== completeDelivery 테스트 ==========

    @Nested
    @DisplayName("completeDelivery 테스트")
    class CompleteDeliveryTest {

        @Test
        @DisplayName("배송 완료 성공 - IN_TRANSIT 상태에서")
        void completeDelivery_배송중상태_완료성공() {
            // Given
            ReflectionTestUtils.setField(testDelivery, "status", DeliveryStatus.IN_TRANSIT);
            given(deliveryRepository.findById(1L)).willReturn(Optional.of(testDelivery));
            given(deliveryRepository.save(any(Delivery.class))).willAnswer(invocation -> invocation.getArgument(0));

            // When
            DeliveryResponse response = deliveryService.completeDelivery(1L);

            // Then
            assertThat(response.getStatus()).isEqualTo(DeliveryStatus.DELIVERED);
            verify(deliveryRepository).save(any(Delivery.class));
        }

        @Test
        @DisplayName("배송 완료 성공 - OUT_FOR_DELIVERY 상태에서")
        void completeDelivery_배송출발상태_완료성공() {
            // Given
            ReflectionTestUtils.setField(testDelivery, "status", DeliveryStatus.OUT_FOR_DELIVERY);
            given(deliveryRepository.findById(1L)).willReturn(Optional.of(testDelivery));
            given(deliveryRepository.save(any(Delivery.class))).willAnswer(invocation -> invocation.getArgument(0));

            // When
            DeliveryResponse response = deliveryService.completeDelivery(1L);

            // Then
            assertThat(response.getStatus()).isEqualTo(DeliveryStatus.DELIVERED);
        }

        @Test
        @DisplayName("배송 준비중 상태에서 완료 시도 시 예외")
        void completeDelivery_준비중상태_예외() {
            // Given
            // testDelivery is already in PREPARING status from setUp()
            given(deliveryRepository.findById(1L)).willReturn(Optional.of(testDelivery));

            // When & Then
            assertThatThrownBy(() -> deliveryService.completeDelivery(1L))
                    .isInstanceOf(DeliveryException.InvalidDeliveryStateException.class)
                    .hasMessageContaining("배송중인 상태에서만");
        }

        @Test
        @DisplayName("존재하지 않는 배송 완료 시도 시 예외")
        void completeDelivery_존재하지않는배송_예외() {
            // Given
            given(deliveryRepository.findById(999L)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> deliveryService.completeDelivery(999L))
                    .isInstanceOf(DeliveryException.DeliveryNotFoundException.class);
        }
    }

    // ========== updateDeliveryStatus 테스트 ==========

    @Nested
    @DisplayName("updateDeliveryStatus 테스트")
    class UpdateDeliveryStatusTest {

        @Test
        @DisplayName("배송 상태 업데이트 성공")
        void updateDeliveryStatus_상태변경_성공() {
            // Given
            given(deliveryRepository.findById(1L)).willReturn(Optional.of(testDelivery));
            given(deliveryRepository.save(any(Delivery.class))).willAnswer(invocation -> invocation.getArgument(0));

            // When
            DeliveryResponse response = deliveryService.updateDeliveryStatus(1L, DeliveryStatus.IN_TRANSIT);

            // Then
            assertThat(response.getStatus()).isEqualTo(DeliveryStatus.IN_TRANSIT);
            verify(deliveryRepository).save(any(Delivery.class));
        }

        @Test
        @DisplayName("존재하지 않는 배송 상태 업데이트 시 예외")
        void updateDeliveryStatus_존재하지않는배송_예외() {
            // Given
            given(deliveryRepository.findById(999L)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> deliveryService.updateDeliveryStatus(999L, DeliveryStatus.IN_TRANSIT))
                    .isInstanceOf(DeliveryException.DeliveryNotFoundException.class);
        }
    }

    // ========== changeAddress 테스트 ==========

    @Nested
    @DisplayName("changeAddress 테스트")
    class ChangeAddressTest {

        @Test
        @DisplayName("배송지 주소 변경 성공")
        void changeAddress_준비중상태_주소변경성공() {
            // Given
            // testDelivery is already in PREPARING status from setUp()
            given(deliveryRepository.findById(1L)).willReturn(Optional.of(testDelivery));
            given(deliveryRepository.save(any(Delivery.class))).willAnswer(invocation -> invocation.getArgument(0));

            // When
            DeliveryResponse response = deliveryService.changeAddress(1L, "12345", "서울시 강북구", "101동 101호");

            // Then
            assertThat(response.getZipCode()).isEqualTo("12345");
            assertThat(response.getAddress()).isEqualTo("서울시 강북구");
            assertThat(response.getAddressDetail()).isEqualTo("101동 101호");
            verify(deliveryRepository).save(any(Delivery.class));
        }

        @Test
        @DisplayName("배송중 상태에서 주소 변경 시 예외")
        void changeAddress_배송중상태_예외() {
            // Given
            ReflectionTestUtils.setField(testDelivery, "status", DeliveryStatus.IN_TRANSIT);
            given(deliveryRepository.findById(1L)).willReturn(Optional.of(testDelivery));

            // When & Then
            assertThatThrownBy(() -> deliveryService.changeAddress(1L, "12345", "서울시 강북구", "101동"))
                    .isInstanceOf(DeliveryException.AddressChangeNotAllowedException.class)
                    .hasMessageContaining("배송 준비중일 때만");
        }

        @Test
        @DisplayName("존재하지 않는 배송의 주소 변경 시 예외")
        void changeAddress_존재하지않는배송_예외() {
            // Given
            given(deliveryRepository.findById(999L)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> deliveryService.changeAddress(999L, "12345", "서울시", "101동"))
                    .isInstanceOf(DeliveryException.DeliveryNotFoundException.class);
        }
    }
}