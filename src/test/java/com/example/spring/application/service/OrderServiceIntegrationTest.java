package com.example.spring.application.service;

import com.example.spring.application.dto.request.CreateOrderRequest;
import com.example.spring.application.dto.request.DeliveryRequest;
import com.example.spring.application.dto.request.OrderItemRequest;
import com.example.spring.application.dto.request.PaymentRequest;
import com.example.spring.application.dto.response.OrderResponse;
import com.example.spring.domain.model.*;
import com.example.spring.exception.BookException;
import com.example.spring.exception.OrderException;
import com.example.spring.domain.repository.*;
import com.example.spring.application.EmailService;
import com.example.spring.application.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.spring.domain.vo.ISBN;
import com.example.spring.domain.vo.Money;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.PlatformTransactionManager;

@SpringBootTest
@Transactional
class OrderServiceIntegrationTest {

    @Autowired private OrderService orderService;
    @Autowired private BookRepository bookRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private MemberRepository memberRepository;
    @Autowired private OrderItemRepository orderItemRepository;
    @Autowired private PaymentRepository paymentRepository;
    @Autowired private DeliveryRepository deliveryRepository;
    @Autowired private LoanRepository loanRepository;
    @Autowired private RefundRepository refundRepository;
    @Autowired private PlatformTransactionManager transactionManager;

    @MockitoBean private EmailService emailService;

    @PersistenceContext private EntityManager entityManager;

    private Book testBook1;
    private Book testBook2;
    private Book testBook3;
    private Member testMember;

    @BeforeEach
    void setUp() {
        new TransactionTemplate(transactionManager).execute(status -> {
            // 기존 데이터 정리 (순서 중요: 자식 테이블부터 삭제)
            // NOT_SUPPORTED 트랜잭션에서 실행될 경우를 대비해 repository 메서드 사용 (내부적으로 트랜잭션 처리됨)
            orderItemRepository.deleteAll();
            paymentRepository.deleteAll();
            deliveryRepository.deleteAll();
            refundRepository.deleteAll();
            orderRepository.deleteAll();
            loanRepository.deleteAll();
            bookRepository.findAll().forEach(book -> bookRepository.deleteById(book.getId()));
            memberRepository.findAll().forEach(member -> memberRepository.deleteById(member.getId()));

            entityManager.flush();
            entityManager.clear();

            // 테스트 데이터 생성
            testBook1 = bookRepository.save(Book.builder().title("Clean Code").author("Robert C. Martin").isbn(ISBN.of("9780132350884")).price(Money.of(new BigDecimal("45000"))).available(true).build());
            testBook2 = bookRepository.save(Book.builder().title("Spring in Action").author("Craig Walls").isbn(ISBN.of("9781617294945")).price(Money.of(new BigDecimal("52000"))).available(true).build());
            testBook3 = bookRepository.save(Book.builder().title("Effective Java").author("Joshua Bloch").isbn(ISBN.of("9780134685991")).price(Money.of(new BigDecimal("48000"))).available(true).build());

            testMember = memberRepository.save(Member.builder()
                    .name("테스트유저")
                    .email("testuser@example.com")
                    .password("test-password")
                    .role(Role.USER)
                    .membershipType(MembershipType.REGULAR)
                    .build());
            return null;
        });
    }

    @Test
    void createOrder_정상주문_실제DB검증() {
        // Given
        CreateOrderRequest request = createOrderRequest(testMember.getId(), List.of(
                createOrderItemRequest(testBook1.getId(), 1)
        ));

        // When
        OrderResponse result = orderService.createOrder(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getTotalAmount()).isEqualByComparingTo(new BigDecimal("45000"));
        assertThat(result.getOrderItems()).hasSize(1);

        Optional<Order> savedOrder = orderRepository.findById(result.getId());
        assertThat(savedOrder).isPresent();

        Order dbOrder = savedOrder.get();
        assertThat(dbOrder.getTotalAmount().getAmount()).isEqualByComparingTo(new BigDecimal("45000"));
        assertThat(dbOrder.getMember().getId()).isEqualTo(testMember.getId());

        OrderItem orderItem = dbOrder.getOrderItems().get(0);
        assertThat(orderItem.getBook().getId()).isEqualTo(testBook1.getId());
        assertThat(orderItem.getPrice().getAmount()).isEqualByComparingTo(new BigDecimal("45000"));
        assertThat(orderItem.getQuantity()).isEqualTo(1);

        verify(emailService).sendOrderConfirmation(any(Order.class));
    }

    @Test
    void createOrder_여러도서주문_실제DB검증() {
        // Given
        CreateOrderRequest request = createOrderRequest(testMember.getId(), List.of(
                createOrderItemRequest(testBook1.getId(), 1),
                createOrderItemRequest(testBook2.getId(), 1),
                createOrderItemRequest(testBook3.getId(), 1)
        ));

        // When
        OrderResponse result = orderService.createOrder(request);

        // Then
        assertThat(result.getTotalAmount()).isEqualByComparingTo(new BigDecimal("145000"));
        assertThat(result.getOrderItems()).hasSize(3);

        entityManager.flush();
        entityManager.clear();

        Order dbOrder = orderRepository.findById(result.getId()).orElseThrow();
        assertThat(dbOrder.getOrderItems()).hasSize(3);

        List<OrderItem> orderItems = dbOrder.getOrderItems();
        BigDecimal totalFromItems = orderItems.stream()
                .map(item -> item.getPrice().getAmount().multiply(new BigDecimal(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        assertThat(totalFromItems).isEqualByComparingTo(new BigDecimal("145000"));
    }

    @Test
    void createOrder_존재하지않는도서_실제DB검증() {
        // Given
        CreateOrderRequest request = createOrderRequest(testMember.getId(), List.of(
                createOrderItemRequest(999999L, 1)
        ));

        // When & Then
        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(BookException.BookNotFoundException.class)
                .hasMessageContaining("도서를 찾을 수 없습니다");

        List<Order> allOrders = orderRepository.findAll();
        assertThat(allOrders).isEmpty();

        verify(emailService, never()).sendOrderConfirmation(any(Order.class));
    }

    @Test
    void createOrder_빈주문목록_실제DB검증() {
        // Given
        CreateOrderRequest request = createOrderRequest(testMember.getId(), Collections.emptyList());

        // When & Then
        OrderResponse result = orderService.createOrder(request);

        assertThat(result.getTotalAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getOrderItems()).isEmpty();
    }

    @Test
    void createOrder_중복도서주문_실제DB검증() {
        // Given
        CreateOrderRequest request = createOrderRequest(testMember.getId(), List.of(
                createOrderItemRequest(testBook1.getId(), 1),
                createOrderItemRequest(testBook1.getId(), 1)
        ));

        // When
        OrderResponse result = orderService.createOrder(request);

        // Then
        BigDecimal expectedTotal = testBook1.getPrice().getAmount().multiply(new BigDecimal("2"));
        assertThat(result.getTotalAmount()).isEqualByComparingTo(expectedTotal);
        assertThat(result.getOrderItems()).hasSize(2);

        Order dbOrder = orderRepository.findById(result.getId()).orElseThrow();
        assertThat(dbOrder.getOrderItems()).hasSize(2);
    }

    @Test
    void createOrder_혼합된주문목록_실제DB검증() {
        // Given
        CreateOrderRequest request = createOrderRequest(testMember.getId(), List.of(
                createOrderItemRequest(testBook1.getId(), 1),
                createOrderItemRequest(999999L, 1)
        ));

        // When & Then
        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(BookException.BookNotFoundException.class);

        List<Order> allOrders = orderRepository.findAll();
        assertThat(allOrders).isEmpty();
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void createOrder_트랜잭션롤백테스트() {
        // Given
        // NOT_SUPPORTED 환경에서는 setUp() 데이터가 보이지 않거나 초기화되지 않을 수 있으므로 직접 생성
        // Valid ISBN-13 format
        String uniqueIsbn = "978" + String.format("%010d", System.currentTimeMillis() % 10000000000L);
        Book book = bookRepository.save(Book.builder().title("Rollback Test Book").author("Author").isbn(ISBN.of(uniqueIsbn)).price(Money.of(new BigDecimal("10000"))).available(true).build());
        Member member = memberRepository.save(Member.builder()
                .name("Rollback User")
                .email("rb-" + System.currentTimeMillis() + "@test.com")
                .password("test-password")
                .role(Role.USER)
                .membershipType(MembershipType.REGULAR)
                .build());

        CreateOrderRequest request = createOrderRequest(member.getId(), List.of(
                createOrderItemRequest(book.getId(), 1)
        ));

        doThrow(new RuntimeException("이메일 발송 실패"))
                .when(emailService).sendOrderConfirmation(any(Order.class));

        // When & Then
        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("이메일 발송 실패");

        // 트랜잭션이 롤백되었으므로 해당 멤버의 주문이 없어야 함
        List<Order> allOrders = orderRepository.findAll();
        boolean orderExists = allOrders.stream()
                .anyMatch(o -> o.getMember().getId().equals(member.getId()));
        assertThat(orderExists).isFalse();

        // Cleanup
        bookRepository.deleteById(book.getId());
        memberRepository.deleteById(member.getId());
    }

    @Test
    void findOrderById_실제DB조회() {
        // Given
        CreateOrderRequest request = createOrderRequest(testMember.getId(), List.of(
                createOrderItemRequest(testBook1.getId(), 1)
        ));
        OrderResponse savedOrder = orderService.createOrder(request);

        entityManager.flush();
        entityManager.clear();

        // When
        OrderResponse foundOrder = orderService.findOrderById(savedOrder.getId());

        // Then
        assertThat(foundOrder).isNotNull();
        assertThat(foundOrder.getId()).isEqualTo(savedOrder.getId());
        assertThat(foundOrder.getTotalAmount()).isEqualByComparingTo(testBook1.getPrice().getAmount());
    }

    @Test
    void findOrderById_존재하지않는주문_실제DB조회() {
        Long nonExistentId = 999999L;

        assertThatThrownBy(() -> orderService.findOrderById(nonExistentId))
                .isInstanceOf(OrderException.OrderNotFoundException.class)
                .hasMessageContaining("주문");
    }

    @Test
    void findAllOrders_실제DB조회() {
        // Given
        CreateOrderRequest request1 = createOrderRequest(testMember.getId(), List.of(
                createOrderItemRequest(testBook1.getId(), 1)
        ));
        OrderResponse order1 = orderService.createOrder(request1);

        CreateOrderRequest request2 = createOrderRequest(testMember.getId(), List.of(
                createOrderItemRequest(testBook2.getId(), 1),
                createOrderItemRequest(testBook3.getId(), 1)
        ));
        OrderResponse order2 = orderService.createOrder(request2);

        entityManager.flush();

        // When
        List<OrderResponse> allOrders = orderService.findAllOrders();

        // Then
        assertThat(allOrders).hasSizeGreaterThanOrEqualTo(2);
        assertThat(allOrders).extracting(OrderResponse::getId)
                .contains(order1.getId(), order2.getId());
    }

    @Test
    void createOrder_DB제약조건검증() {
        // Given
        CreateOrderRequest request = createOrderRequest(testMember.getId(), List.of(
                createOrderItemRequest(testBook1.getId(), 1)
        ));

        // When
        OrderResponse result = orderService.createOrder(request);

        // Then
        entityManager.flush();

        assertThat(result.getId()).isNotNull();
        assertThat(result.getTotalAmount()).isNotNull();
        assertThat(result.getOrderDate()).isNotNull();

        Order dbOrder = orderRepository.findById(result.getId()).orElseThrow();
        Book relatedBook = dbOrder.getOrderItems().get(0).getBook();
        assertThat(relatedBook.getTitle()).isEqualTo("Clean Code");
    }

    // --- Helper Methods ---
    private OrderItemRequest createOrderItemRequest(Long bookId, Integer quantity) {
        return OrderItemRequest.builder()
                .bookId(bookId)
                .quantity(quantity)
                .build();
    }

    private CreateOrderRequest createOrderRequest(Long memberId, List<OrderItemRequest> items) {
        PaymentRequest payment = PaymentRequest.builder()
                .method(PaymentMethod.CREDIT_CARD)
                .amount(new BigDecimal("10000"))
                .build();

        DeliveryRequest delivery = DeliveryRequest.builder()
                .recipientName("홍길동")
                .phoneNumber("010-1234-5678")
                .address("서울시 강남구")
                .build();

        return CreateOrderRequest.builder()
                .memberId(memberId)
                .items(items)
                .payment(payment)
                .delivery(delivery)
                .build();
    }
}