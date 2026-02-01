package com.example.spring.repository;

import com.example.spring.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("DeliveryRepository 테스트")
public class DeliveryRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private DeliveryRepository deliveryRepository;

    private Member testMember;
    private Order testOrder;
    private static long uniqueCounter = 0;

    @BeforeEach
    void setUp() {
        testMember = Member.builder()
                .name("Test Member")
                .email("delivery" + (++uniqueCounter) + "@test.com")
                .password("password")
                .role(Role.USER)
                .membershipType(MembershipType.REGULAR)
                .joinDate(LocalDateTime.now())
                .build();
        entityManager.persist(testMember);

        testOrder = Order.builder()
                .member(testMember)
                .totalAmount(new BigDecimal("50000"))
                .orderDate(LocalDateTime.now())
                .status(OrderStatus.PENDING)
                .build();
        entityManager.persist(testOrder);
        entityManager.flush();
    }

    private Delivery createDelivery(Order order, DeliveryStatus status, String recipientName,
                                    String trackingNumber, String courierCompany) {
        Delivery delivery = Delivery.builder()
                .order(order)
                .recipientName(recipientName)
                .phoneNumber("010-1234-5678")
                .address("서울시 강남구 테헤란로 123")
                .status(status)
                .trackingNumber(trackingNumber)
                .courierCompany(courierCompany)
                .build();
        return entityManager.persistAndFlush(delivery);
    }

    // ========== 기본 CRUD 테스트 ==========

    @Nested
    @DisplayName("기본 CRUD 테스트")
    class BasicCrudTest {

        @Test
        @DisplayName("배송 저장 성공")
        void save_배송저장_성공() {
            // Given
            Delivery delivery = Delivery.builder()
                    .order(testOrder)
                    .recipientName("홍길동")
                    .phoneNumber("010-1234-5678")
                    .address("서울시 강남구")
                    .status(DeliveryStatus.PREPARING)
                    .build();

            // When
            Delivery saved = deliveryRepository.save(delivery);

            // Then
            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getRecipientName()).isEqualTo("홍길동");
            assertThat(saved.getStatus()).isEqualTo(DeliveryStatus.PREPARING);
        }

        @Test
        @DisplayName("배송 ID로 조회 성공")
        void findById_존재하는배송_조회성공() {
            // Given
            Delivery delivery = createDelivery(testOrder, DeliveryStatus.PREPARING,
                    "홍길동", null, null);

            // When
            Optional<Delivery> found = deliveryRepository.findById(delivery.getId());

            // Then
            assertThat(found).isPresent();
            assertThat(found.get().getRecipientName()).isEqualTo("홍길동");
        }

        @Test
        @DisplayName("존재하지 않는 배송 조회 시 빈 Optional")
        void findById_존재하지않는배송_빈Optional() {
            // When
            Optional<Delivery> found = deliveryRepository.findById(999L);

            // Then
            assertThat(found).isEmpty();
        }
    }

    // ========== 주문 ID로 조회 테스트 ==========

    @Nested
    @DisplayName("findByOrderId 테스트")
    class FindByOrderIdTest {

        @Test
        @DisplayName("주문 ID로 배송 조회 성공")
        void findByOrderId_존재하는주문_배송반환() {
            // Given
            createDelivery(testOrder, DeliveryStatus.PREPARING, "김철수", null, null);

            // When
            Optional<Delivery> found = deliveryRepository.findByOrderId(testOrder.getId());

            // Then
            assertThat(found).isPresent();
            assertThat(found.get().getOrder().getId()).isEqualTo(testOrder.getId());
        }

        @Test
        @DisplayName("존재하지 않는 주문 ID로 조회 시 빈 Optional")
        void findByOrderId_존재하지않는주문_빈Optional() {
            // When
            Optional<Delivery> found = deliveryRepository.findByOrderId(999L);

            // Then
            assertThat(found).isEmpty();
        }
    }

    // ========== 운송장 번호로 조회 테스트 ==========

    @Nested
    @DisplayName("findByTrackingNumber 테스트")
    class FindByTrackingNumberTest {

        @Test
        @DisplayName("운송장 번호로 배송 조회 성공")
        void findByTrackingNumber_존재하는운송장_배송반환() {
            // Given
            String trackingNumber = "123456789012";
            createDelivery(testOrder, DeliveryStatus.IN_TRANSIT, "홍길동",
                    trackingNumber, "CJ대한통운");

            // When
            Optional<Delivery> found = deliveryRepository.findByTrackingNumber(trackingNumber);

            // Then
            assertThat(found).isPresent();
            assertThat(found.get().getTrackingNumber()).isEqualTo(trackingNumber);
            assertThat(found.get().getCourierCompany()).isEqualTo("CJ대한통운");
        }

        @Test
        @DisplayName("존재하지 않는 운송장 번호로 조회 시 빈 Optional")
        void findByTrackingNumber_존재하지않는운송장_빈Optional() {
            // When
            Optional<Delivery> found = deliveryRepository.findByTrackingNumber("NON-EXISTENT");

            // Then
            assertThat(found).isEmpty();
        }
    }

    // ========== 상태별 조회 테스트 ==========

    @Nested
    @DisplayName("findByStatus 테스트")
    class FindByStatusTest {

        @Test
        @DisplayName("상태별 배송 조회")
        void findByStatus_상태별배송_조회성공() {
            // Given
            createDelivery(testOrder, DeliveryStatus.PREPARING, "홍길동", null, null);

            Order order2 = createNewOrder();
            createDelivery(order2, DeliveryStatus.IN_TRANSIT, "김영희",
                    "TRK001", "CJ대한통운");

            Order order3 = createNewOrder();
            createDelivery(order3, DeliveryStatus.IN_TRANSIT, "박철수",
                    "TRK002", "우체국");

            // When
            List<Delivery> preparingDeliveries = deliveryRepository.findByStatus(DeliveryStatus.PREPARING);
            List<Delivery> inTransitDeliveries = deliveryRepository.findByStatus(DeliveryStatus.IN_TRANSIT);

            // Then
            assertThat(preparingDeliveries).hasSize(1);
            assertThat(inTransitDeliveries).hasSize(2);
        }

        @Test
        @DisplayName("해당 상태 배송 없으면 빈 리스트")
        void findByStatus_없는상태_빈리스트() {
            // Given
            createDelivery(testOrder, DeliveryStatus.PREPARING, "홍길동", null, null);

            // When
            List<Delivery> deliveredList = deliveryRepository.findByStatus(DeliveryStatus.DELIVERED);

            // Then
            assertThat(deliveredList).isEmpty();
        }
    }

    // ========== 수령인 이름으로 조회 테스트 ==========

    @Nested
    @DisplayName("findByRecipientNameContaining 테스트")
    class FindByRecipientNameContainingTest {

        @Test
        @DisplayName("수령인 이름 부분 검색")
        void findByRecipientNameContaining_부분일치_조회성공() {
            // Given
            createDelivery(testOrder, DeliveryStatus.PREPARING, "홍길동", null, null);

            Order order2 = createNewOrder();
            createDelivery(order2, DeliveryStatus.PREPARING, "홍길순", null, null);

            Order order3 = createNewOrder();
            createDelivery(order3, DeliveryStatus.PREPARING, "김철수", null, null);

            // When
            List<Delivery> hongDeliveries = deliveryRepository.findByRecipientNameContaining("홍");
            List<Delivery> gilDeliveries = deliveryRepository.findByRecipientNameContaining("길");

            // Then
            assertThat(hongDeliveries).hasSize(2);
            assertThat(gilDeliveries).hasSize(2); // 홍길동, 홍길순
        }

        @Test
        @DisplayName("일치하는 수령인 없으면 빈 리스트")
        void findByRecipientNameContaining_없음_빈리스트() {
            // Given
            createDelivery(testOrder, DeliveryStatus.PREPARING, "홍길동", null, null);

            // When
            List<Delivery> result = deliveryRepository.findByRecipientNameContaining("박");

            // Then
            assertThat(result).isEmpty();
        }
    }

    // ========== 택배사로 조회 테스트 ==========

    @Nested
    @DisplayName("findByCourierCompany 테스트")
    class FindByCourierCompanyTest {

        @Test
        @DisplayName("택배사별 배송 조회")
        void findByCourierCompany_택배사별_조회성공() {
            // Given
            createDelivery(testOrder, DeliveryStatus.IN_TRANSIT, "홍길동",
                    "TRK001", "CJ대한통운");

            Order order2 = createNewOrder();
            createDelivery(order2, DeliveryStatus.IN_TRANSIT, "김영희",
                    "TRK002", "CJ대한통운");

            Order order3 = createNewOrder();
            createDelivery(order3, DeliveryStatus.IN_TRANSIT, "박철수",
                    "TRK003", "우체국");

            // When
            List<Delivery> cjDeliveries = deliveryRepository.findByCourierCompany("CJ대한통운");
            List<Delivery> postDeliveries = deliveryRepository.findByCourierCompany("우체국");

            // Then
            assertThat(cjDeliveries).hasSize(2);
            assertThat(postDeliveries).hasSize(1);
        }

        @Test
        @DisplayName("해당 택배사 배송 없으면 빈 리스트")
        void findByCourierCompany_없는택배사_빈리스트() {
            // Given
            createDelivery(testOrder, DeliveryStatus.IN_TRANSIT, "홍길동",
                    "TRK001", "CJ대한통운");

            // When
            List<Delivery> result = deliveryRepository.findByCourierCompany("한진택배");

            // Then
            assertThat(result).isEmpty();
        }
    }

    // ========== 상태와 주문 ID로 조회 테스트 ==========

    @Nested
    @DisplayName("findByStatusAndOrderId 테스트")
    class FindByStatusAndOrderIdTest {

        @Test
        @DisplayName("배송 준비중인 특정 주문 배송 조회")
        void findByStatusAndOrderId_일치_조회성공() {
            // Given
            createDelivery(testOrder, DeliveryStatus.PREPARING, "홍길동", null, null);

            // When
            List<Delivery> result = deliveryRepository.findByStatusAndOrderId(
                    DeliveryStatus.PREPARING, testOrder.getId());

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).canChangeAddress()).isTrue();
        }

        @Test
        @DisplayName("상태 불일치 시 빈 리스트")
        void findByStatusAndOrderId_상태불일치_빈리스트() {
            // Given
            createDelivery(testOrder, DeliveryStatus.IN_TRANSIT, "홍길동",
                    "TRK001", "CJ대한통운");

            // When
            List<Delivery> result = deliveryRepository.findByStatusAndOrderId(
                    DeliveryStatus.PREPARING, testOrder.getId());

            // Then
            assertThat(result).isEmpty();
        }
    }

    // ========== 삭제 테스트 ==========

    @Nested
    @DisplayName("delete 테스트")
    class DeleteTest {

        @Test
        @DisplayName("배송 삭제 성공")
        void delete_배송삭제_성공() {
            // Given
            Delivery delivery = createDelivery(testOrder, DeliveryStatus.PREPARING,
                    "홍길동", null, null);
            Long deliveryId = delivery.getId();

            // When
            deliveryRepository.deleteById(deliveryId);
            entityManager.flush();

            // Then
            assertThat(entityManager.find(Delivery.class, deliveryId)).isNull();
        }
    }

    // ========== Helper Methods ==========

    private Order createNewOrder() {
        Order order = Order.builder()
                .member(testMember)
                .totalAmount(new BigDecimal("30000"))
                .orderDate(LocalDateTime.now())
                .status(OrderStatus.PENDING)
                .build();
        return entityManager.persistAndFlush(order);
    }
}
