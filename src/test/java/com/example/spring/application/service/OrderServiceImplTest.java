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
import com.example.spring.application.LoggingService;
import com.example.spring.application.service.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.spring.domain.vo.ISBN;
import com.example.spring.domain.vo.Money;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock private BookRepository bookRepository;
    @Mock private OrderRepository orderRepository;
    @Mock private MemberRepository memberRepository;
    @Mock private PaymentRepository paymentRepository;
    @Mock private DeliveryRepository deliveryRepository;
    @Mock private EmailService emailService;
    @Mock private LoggingService loggingService;
    @Mock private ApplicationEventPublisher eventPublisher;

    private OrderServiceImpl orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderServiceImpl(
                bookRepository,
                memberRepository,
                orderRepository,
                paymentRepository,
                deliveryRepository,
                emailService,
                loggingService,
                eventPublisher
        );
    }

    @Test
    void createOrder_정상주문() {
        // Given
        Long memberId = 1L;
        Book book = createTestBook(1L, "테스트책", new BigDecimal("10000"));
        Member member = createTestMember(memberId);

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            ReflectionTestUtils.setField(order, "id", 1L); // ID 설정
            return order;
        });

        CreateOrderRequest request = createOrderRequest(memberId, List.of(createOrderItemRequest(1L, 1)));

        // When
        OrderResponse result = orderService.createOrder(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTotalAmount()).isEqualByComparingTo(new BigDecimal("10000"));
        assertThat(result.getOrderDate()).isNotNull();

        verify(bookRepository).findById(1L);
        verify(memberRepository).findById(memberId);
        verify(orderRepository).save(any(Order.class));
        verify(emailService).sendOrderConfirmation(any(Order.class));
        verify(loggingService).log(contains("주문 생성 시작"));
        verify(loggingService).log(contains("주문 생성 완료"));
    }

    @Test
    void createOrder_여러도서주문() {
        // Given
        Long memberId = 1L;
        Book book1 = createTestBook(1L, "테스트책1", new BigDecimal("10000"));
        Book book2 = createTestBook(2L, "테스트책2", new BigDecimal("15000"));
        Member member = createTestMember(memberId);

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book1));
        when(bookRepository.findById(2L)).thenReturn(Optional.of(book2));
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            ReflectionTestUtils.setField(order, "id", 1L);
            return order;
        });

        CreateOrderRequest request = createOrderRequest(memberId, List.of(
                createOrderItemRequest(1L, 1),
                createOrderItemRequest(2L, 1)
        ));

        // When
        OrderResponse result = orderService.createOrder(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTotalAmount()).isEqualByComparingTo(new BigDecimal("25000")); // 10000 + 15000

        verify(bookRepository).findById(1L);
        verify(bookRepository).findById(2L);
        verify(memberRepository).findById(memberId);
        verify(orderRepository).save(any(Order.class));
        verify(emailService).sendOrderConfirmation(any(Order.class));
    }

    @Test
    void createOrder_존재하지않는도서() {
        // Given
        Long memberId = 1L;
        Member member = createTestMember(memberId);
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(bookRepository.findById(999L)).thenReturn(Optional.empty());

        CreateOrderRequest request = createOrderRequest(memberId, List.of(createOrderItemRequest(999L, 1)));

        // When & Then
        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(BookException.BookNotFoundException.class)
                .hasMessageContaining("도서를 찾을 수 없습니다");

        verify(memberRepository).findById(memberId);
        verify(bookRepository).findById(999L);
        verify(orderRepository, never()).save(any(Order.class));
        verify(emailService, never()).sendOrderConfirmation(any(Order.class));
        verify(loggingService).error(eq("주문 생성 실패"), any(Exception.class));
    }

    @Test
    void createOrder_빈주문목록() {
        // Given
        Long memberId = 1L;
        Member member = createTestMember(memberId);
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

        // 빈 리스트라도 저장은 수행됨 (금액 0)
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            ReflectionTestUtils.setField(order, "id", 1L);
            return order;
        });

        CreateOrderRequest request = createOrderRequest(memberId, Collections.emptyList());

        // When
        OrderResponse result = orderService.createOrder(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTotalAmount()).isEqualByComparingTo(BigDecimal.ZERO);

        verify(memberRepository).findById(memberId);
        verify(bookRepository, never()).findById(anyLong());
        verify(orderRepository).save(any(Order.class));
        verify(emailService).sendOrderConfirmation(any(Order.class));
    }

    @Test
    void createOrder_null입력() {
        // Given - null 입력

        // When & Then
        assertThatThrownBy(() -> orderService.createOrder(null))
                .isInstanceOf(NullPointerException.class);

        verify(loggingService, never()).log(anyString());
    }

    @Test
    void createOrder_혼합된주문목록() {
        // Given
        Long memberId = 1L;
        Book book = createTestBook(1L, "테스트책", new BigDecimal("10000"));
        Member member = createTestMember(memberId);

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookRepository.findById(999L)).thenReturn(Optional.empty());

        CreateOrderRequest request = createOrderRequest(memberId, List.of(
                createOrderItemRequest(1L, 1),
                createOrderItemRequest(999L, 1)
        ));

        // When & Then
        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(BookException.BookNotFoundException.class)
                .hasMessageContaining("도서를 찾을 수 없습니다");

        verify(bookRepository).findById(1L);
        verify(bookRepository).findById(999L);
        verify(orderRepository, never()).save(any(Order.class));
        verify(emailService, never()).sendOrderConfirmation(any(Order.class));
        verify(loggingService).error(eq("주문 생성 실패"), any(Exception.class));
    }

    @Test
    void createOrder_중복도서주문() {
        // Given
        Long memberId = 1L;
        Book book = createTestBook(1L, "테스트책", new BigDecimal("10000"));
        Member member = createTestMember(memberId);

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            ReflectionTestUtils.setField(order, "id", 1L);
            return order;
        });

        CreateOrderRequest request = createOrderRequest(memberId, List.of(
                createOrderItemRequest(1L, 1),
                createOrderItemRequest(1L, 1)
        ));

        // When
        OrderResponse result = orderService.createOrder(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTotalAmount()).isEqualByComparingTo(new BigDecimal("20000")); // 10000 * 2

        verify(bookRepository, times(2)).findById(1L); // 두 번 호출됨
        verify(orderRepository).save(any(Order.class));
        verify(emailService).sendOrderConfirmation(any(Order.class));
    }

    @Test
    void findOrderById_정상조회() {
        // Given
        Order expectedOrder = createTestOrder(1L, new BigDecimal("10000"));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(expectedOrder));

        // When
        OrderResponse result = orderService.findOrderById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTotalAmount()).isEqualByComparingTo(new BigDecimal("10000"));

        verify(orderRepository).findById(1L);
    }

    @Test
    void findOrderById_존재하지않는주문() {
        // Given
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> orderService.findOrderById(999L))
                .isInstanceOf(OrderException.OrderNotFoundException.class)
                .hasMessageContaining("주문");

        verify(orderRepository).findById(999L);
    }

    @Test
    void findAllOrders_전체조회() {
        // Given
        List<Order> expectedOrders = Arrays.asList(
                createTestOrder(1L, new BigDecimal("10000")),
                createTestOrder(2L, new BigDecimal("20000"))
        );
        when(orderRepository.findAll()).thenReturn(expectedOrders);

        // When
        List<OrderResponse> result = orderService.findAllOrders();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTotalAmount()).isEqualByComparingTo(new BigDecimal("10000"));
        assertThat(result.get(1).getTotalAmount()).isEqualByComparingTo(new BigDecimal("20000"));

        verify(orderRepository).findAll();
    }

    // 테스트 헬퍼 메서드들
    private Book createTestBook(Long id, String title, BigDecimal price) {
        return Book.builder()
                .id(id)
                .title(title)
                .author("테스트 저자")
                .isbn(ISBN.of("978" + String.format("%010d", id)))
                .price(Money.of(price))
                .available(true)
                .build();
    }

    private Member createTestMember(Long id) {
        return Member.builder()
                .id(id)
                .name("테스트 회원")
                .email("test@example.com")
                .password("test-password")
                .role(Role.USER)
                .membershipType(MembershipType.REGULAR)
                .build();
    }

    private Order createTestOrder(Long id, BigDecimal totalAmount) {
        return Order.builder()
                .id(id)
                .member(createTestMember(1L))
                .totalAmount(Money.of(totalAmount))
                .orderDate(LocalDateTime.now())
                .build();
    }

    private OrderItemRequest createOrderItemRequest(Long bookId, Integer quantity) {
        return OrderItemRequest.builder()
                .bookId(bookId)
                .quantity(quantity)
                .build();
    }

    private CreateOrderRequest createOrderRequest(Long memberId, List<OrderItemRequest> items) {
        PaymentRequest payment = PaymentRequest.builder()
                .method(PaymentMethod.CREDIT_CARD)
                .amount(new BigDecimal("10000")) // 테스트용 더미 금액
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