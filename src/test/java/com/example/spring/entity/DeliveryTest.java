package com.example.spring.entity;

import com.example.spring.domain.vo.Address;
import com.example.spring.exception.DeliveryException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Delivery 엔티티 테스트")
class DeliveryTest {

    private Delivery testDelivery;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        testOrder = Order.builder()
                .id(1L)
                .build();

        testDelivery = Delivery.builder()
                .id(1L)
                .order(testOrder)
                .recipientName("홍길동")
                .phoneNumber("010-1234-5678")
                .deliveryAddress(Address.of("12345", "서울시 강남구 테헤란로", "101호"))
                .status(DeliveryStatus.PREPARING)
                .build();
    }

    @Nested
    @DisplayName("배송 생성")
    class CreateDeliveryTest {

        @Test
        @DisplayName("배송 정보 생성 - 기본 상태 PREPARING")
        void createDelivery_defaultStatus_preparing() {
            // Given & When
            Delivery delivery = Delivery.builder()
                    .order(testOrder)
                    .recipientName("김철수")
                    .phoneNumber("010-9876-5432")
                    .deliveryAddress(Address.of("54321", "부산시 해운대구", "해운대아파트"))
                    .build();

            // Then
            assertThat(delivery.getStatus()).isEqualTo(DeliveryStatus.PREPARING);
            assertThat(delivery.getRecipientName()).isEqualTo("김철수");
        }

        @Test
        @DisplayName("배송 메모 포함하여 생성")
        void createDelivery_withMemo_success() {
            // Given & When
            Delivery delivery = Delivery.builder()
                    .order(testOrder)
                    .recipientName("홍길동")
                    .phoneNumber("010-1234-5678")
                    .deliveryAddress(Address.of("12345", "서울시 강남구", "101호"))
                    .deliveryMemo("경비실에 맡겨주세요")
                    .build();

            // Then
            assertThat(delivery.getDeliveryMemo()).isEqualTo("경비실에 맡겨주세요");
        }
    }

    @Nested
    @DisplayName("배송 상태 전이")
    class DeliveryStatusTransitionTest {

        @Test
        @DisplayName("배송 시작 - PREPARING -> IN_TRANSIT")
        void startShipping_fromPreparing_success() {
            // Given
            String trackingNumber = "1234567890";
            String courierCompany = "CJ대한통운";

            // When
            testDelivery.startShipping(trackingNumber, courierCompany);

            // Then
            assertThat(testDelivery.getStatus()).isEqualTo(DeliveryStatus.IN_TRANSIT);
            assertThat(testDelivery.getTrackingNumber()).isEqualTo(trackingNumber);
            assertThat(testDelivery.getCourierCompany()).isEqualTo(courierCompany);
            assertThat(testDelivery.getShippedDate()).isNotNull();
        }

        @Test
        @DisplayName("배송 시작 - IN_TRANSIT 상태에서 시도 시 예외")
        void startShipping_fromInTransit_throwsException() {
            // Given
            testDelivery.startShipping("123", "택배사");

            // When & Then
            assertThatThrownBy(() -> testDelivery.startShipping("456", "다른택배사"))
                    .isInstanceOf(DeliveryException.InvalidDeliveryStateException.class)
                    .hasMessageContaining("배송 준비중인 상태에서만");
        }

        @Test
        @DisplayName("배송 완료 - IN_TRANSIT -> DELIVERED")
        void complete_fromInTransit_success() {
            // Given
            testDelivery.startShipping("123", "CJ대한통운");

            // When
            testDelivery.complete();

            // Then
            assertThat(testDelivery.getStatus()).isEqualTo(DeliveryStatus.DELIVERED);
            assertThat(testDelivery.getDeliveredDate()).isNotNull();
        }

        @Test
        @DisplayName("배송 완료 - PREPARING 상태에서 시도 시 예외")
        void complete_fromPreparing_throwsException() {
            // When & Then
            assertThatThrownBy(() -> testDelivery.complete())
                    .isInstanceOf(DeliveryException.InvalidDeliveryStateException.class)
                    .hasMessageContaining("배송중인 상태에서만 완료");
        }

        @Test
        @DisplayName("배송 상태 직접 업데이트")
        void updateStatus_success() {
            // When
            testDelivery.updateStatus(DeliveryStatus.OUT_FOR_DELIVERY);

            // Then
            assertThat(testDelivery.getStatus()).isEqualTo(DeliveryStatus.OUT_FOR_DELIVERY);
        }
    }

    @Nested
    @DisplayName("배송 완료 확인")
    class IsDeliveredTest {

        @Test
        @DisplayName("배송 완료 상태 확인 - true")
        void isDelivered_delivered_true() {
            // Given
            testDelivery.startShipping("123", "택배사");
            testDelivery.complete();

            // When & Then
            assertThat(testDelivery.isDelivered()).isTrue();
        }

        @Test
        @DisplayName("배송 준비 중 - false")
        void isDelivered_preparing_false() {
            assertThat(testDelivery.isDelivered()).isFalse();
        }

        @Test
        @DisplayName("배송 중 - false")
        void isDelivered_inTransit_false() {
            // Given
            testDelivery.startShipping("123", "택배사");

            // When & Then
            assertThat(testDelivery.isDelivered()).isFalse();
        }
    }

    @Nested
    @DisplayName("주소 변경")
    class ChangeAddressTest {

        @Test
        @DisplayName("배송 준비 중 주소 변경 성공")
        void changeAddress_preparing_success() {
            // Given
            assertThat(testDelivery.canChangeAddress()).isTrue();

            // When
            testDelivery.changeAddress("54321", "부산시 해운대구", "마린시티");

            // Then
            assertThat(testDelivery.getDeliveryAddress()).isEqualTo("54321");
            assertThat(testDelivery.getDeliveryAddress()).isEqualTo("부산시 해운대구");
            assertThat(testDelivery.getDeliveryAddress()).isEqualTo("마린시티");
        }

        @Test
        @DisplayName("Address 객체로 주소 변경 성공")
        void changeAddress_withAddressObject_success() {
            // Given
            Address newAddress = Address.of("11111", "대전시 유성구", "대덕테크노밸리");

            // When
            testDelivery.changeAddress(newAddress);

            // Then
            assertThat(testDelivery.getDeliveryAddress()).isEqualTo(newAddress);
        }

        @Test
        @DisplayName("배송 시작 후 주소 변경 불가")
        void changeAddress_afterShipping_throwsException() {
            // Given
            testDelivery.startShipping("123", "택배사");
            assertThat(testDelivery.canChangeAddress()).isFalse();

            // When & Then
            assertThatThrownBy(() -> testDelivery.changeAddress("99999", "변경주소", "상세"))
                    .isInstanceOf(DeliveryException.AddressChangeNotAllowedException.class)
                    .hasMessageContaining("배송 준비중일 때만 주소를 변경");
        }

        @Test
        @DisplayName("주소 변경 가능 여부 확인")
        void canChangeAddress_checkByStatus() {
            // PREPARING
            assertThat(testDelivery.canChangeAddress()).isTrue();

            // IN_TRANSIT
            testDelivery.startShipping("123", "택배사");
            assertThat(testDelivery.canChangeAddress()).isFalse();

            // DELIVERED
            testDelivery.complete();
            assertThat(testDelivery.canChangeAddress()).isFalse();
        }
    }

    @Nested
    @DisplayName("주소 정보 조회")
    class AddressInfoTest {

        @Test
        @DisplayName("전체 주소 조회")
        void getFullAddress_success() {
            // When
            String fullAddress = testDelivery.getFullAddress();

            // Then
            assertThat(fullAddress).contains("12345");
            assertThat(fullAddress).contains("서울시 강남구 테헤란로");
            assertThat(fullAddress).contains("101호");
        }

        @Test
        @DisplayName("개별 주소 정보 조회")
        void getAddressParts_success() {
            assertThat(testDelivery.getZipCode()).isEqualTo("12345");
            assertThat(testDelivery.getDeliveryAddress()).isEqualTo("서울시 강남구 테헤란로");
            assertThat(testDelivery.getAddressDetail()).isEqualTo("101호");
        }

        @Test
        @DisplayName("주소가 null인 경우 null 반환")
        void getAddress_nullAddress_returnsNull() {
            // Given
            Delivery delivery = Delivery.builder()
                    .order(testOrder)
                    .recipientName("테스트")
                    .phoneNumber("010-0000-0000")
                    .build();

            // When & Then
            assertThat(delivery.getZipCode()).isNull();
            assertThat(delivery.getDeliveryAddress()).isNull();
            assertThat(delivery.getFullAddress()).isNull();
        }
    }

    @Nested
    @DisplayName("배송 상태 테스트")
    class DeliveryStatusTest {

        @Test
        @DisplayName("모든 배송 상태 확인")
        void allDeliveryStatuses() {
            assertThat(DeliveryStatus.values()).contains(
                    DeliveryStatus.PREPARING,
                    DeliveryStatus.IN_TRANSIT,
                    DeliveryStatus.OUT_FOR_DELIVERY,
                    DeliveryStatus.DELIVERED,
                    DeliveryStatus.FAILED
            );
        }
    }
}
