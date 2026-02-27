package com.example.spring.presentation.controller;

import com.example.spring.application.dto.request.CreateOrderRequest;
import com.example.spring.application.dto.request.DeliveryRequest;
import com.example.spring.application.dto.request.OrderItemRequest;
import com.example.spring.application.dto.request.PaymentRequest;
import com.example.spring.application.dto.response.OrderItemResponse;
import com.example.spring.application.dto.response.OrderResponse;
import com.example.spring.domain.model.OrderStatus;
import com.example.spring.domain.model.PaymentMethod;
import com.example.spring.exception.OrderException;
import com.example.spring.application.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("OrderController 통합 테스트")
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @Autowired
    private ObjectMapper objectMapper;

    private OrderResponse testOrderResponse;
    private CreateOrderRequest createOrderRequest;

    @BeforeEach
    void setUp() {
        // 테스트용 주문 아이템 응답
        OrderItemResponse orderItem1 = OrderItemResponse.builder()
                .id(1L)
                .bookId(1L)
                .bookTitle("Clean Code")
                .bookAuthor("Robert C. Martin")
                .bookIsbn("9780132350884")
                .quantity(2)
                .price(new BigDecimal("45000"))
                .build();

        OrderItemResponse orderItem2 = OrderItemResponse.builder()
                .id(2L)
                .bookId(2L)
                .bookTitle("Effective Java")
                .bookAuthor("Joshua Bloch")
                .bookIsbn("9780134685991")
                .quantity(1)
                .price(new BigDecimal("38000"))
                .build();

        // 테스트용 주문 응답 데이터
        testOrderResponse = OrderResponse.builder()
                .id(1L)
                .orderItems(List.of(orderItem1, orderItem2))
                .totalAmount(new BigDecimal("128000"))
                .status(OrderStatus.PENDING)
                .orderDate(LocalDateTime.now())
                .build();

        // 주문 요청 DTO 생성
        OrderItemRequest item1 = OrderItemRequest.builder()
                .bookId(1L)
                .quantity(2)
                .build();
        OrderItemRequest item2 = OrderItemRequest.builder()
                .bookId(2L)
                .quantity(1)
                .build();

        PaymentRequest payment = PaymentRequest.builder()
                .method(PaymentMethod.CREDIT_CARD)
                .amount(new BigDecimal("128000"))
                .build();

        DeliveryRequest delivery = DeliveryRequest.builder()
                .recipientName("홍길동")
                .phoneNumber("010-1234-5678")
                .address("서울시 강남구")
                .build();

        createOrderRequest = CreateOrderRequest.builder()
                .memberId(1L)
                .items(List.of(item1, item2))
                .payment(payment)
                .delivery(delivery)
                .build();
    }

    @Nested
    @DisplayName("주문 생성")
    class CreateOrderTest {

        @Test
        @DisplayName("주문 생성 성공")
        void createOrder_유효한요청_생성성공() throws Exception {
            // Given
            given(orderService.createOrder(any(CreateOrderRequest.class))).willReturn(testOrderResponse);

            // When & Then
            mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createOrderRequest)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.status").value("PENDING"))
                    .andExpect(jsonPath("$.totalAmount").value(128000))
                    .andExpect(jsonPath("$.orderItems").isArray())
                    .andExpect(jsonPath("$.orderItems.length()").value(2));

            verify(orderService).createOrder(any(CreateOrderRequest.class));
        }

        @Test
        @DisplayName("빈 도서 목록으로 주문 생성 시 400 에러")
        void createOrder_빈도서목록_400에러() throws Exception {
            // Given
            CreateOrderRequest emptyRequest = CreateOrderRequest.builder()
                    .memberId(1L)
                    .items(Collections.emptyList()) // 빈 리스트
                    .payment(createOrderRequest.getPayment())
                    .delivery(createOrderRequest.getDelivery())
                    .build();

            // When & Then
            // DTO 검증(@NotEmpty)에 의해 400 Bad Request가 발생해야 함
            mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(emptyRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("주문 조회")
    class GetOrderTest {

        @Test
        @DisplayName("전체 주문 목록 조회 성공 (페이징)")
        void getAllOrders_목록조회_성공() throws Exception {
            // Given
            List<OrderResponse> orders = List.of(testOrderResponse);
            Page<OrderResponse> orderPage = new PageImpl<>(orders);
            given(orderService.findAllOrdersWithPagination(any(Pageable.class))).willReturn(orderPage);

            // When & Then
            mockMvc.perform(get("/api/orders")
                            .param("page", "0")
                            .param("size", "10"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content.length()").value(1))
                    .andExpect(jsonPath("$.content[0].id").value(1L))
                    .andExpect(jsonPath("$.content[0].status").value("PENDING"))
                    .andExpect(jsonPath("$.totalElements").value(1));

            verify(orderService).findAllOrdersWithPagination(any(Pageable.class));
        }

        @Test
        @DisplayName("특정 주문 조회 성공")
        void getOrder_존재하는ID_조회성공() throws Exception {
            // Given
            given(orderService.findOrderById(1L)).willReturn(testOrderResponse);

            // When & Then
            mockMvc.perform(get("/api/orders/1"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.status").value("PENDING"));

            verify(orderService).findOrderById(1L);
        }

        @Test
        @DisplayName("존재하지 않는 주문 조회 시 404 에러")
        void getOrder_존재하지않는ID_404에러() throws Exception {
            // Given
            given(orderService.findOrderById(999L))
                    .willThrow(new OrderException.OrderNotFoundException(999L));

            // When & Then
            mockMvc.perform(get("/api/orders/999"))
                    .andDo(print())
                    .andExpect(status().isNotFound());

            verify(orderService).findOrderById(999L);
        }
    }

    @Nested
    @DisplayName("주문 상태 관리")
    class OrderStatusManagementTest {

        @Test
        @DisplayName("주문 확인 성공")
        void confirmOrder_성공() throws Exception {
            // Given
            OrderResponse confirmedOrder = OrderResponse.builder()
                    .id(1L)
                    .orderItems(testOrderResponse.getOrderItems())
                    .totalAmount(testOrderResponse.getTotalAmount())
                    .status(OrderStatus.CONFIRMED)
                    .orderDate(testOrderResponse.getOrderDate())
                    .build();

            given(orderService.confirmOrder(1L)).willReturn(confirmedOrder);

            // When & Then
            mockMvc.perform(patch("/api/orders/1/confirm"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("CONFIRMED"));

            verify(orderService).confirmOrder(1L);
        }

        @Test
        @DisplayName("주문 취소 성공")
        void cancelOrder_유효한요청_성공() throws Exception {
            // Given
            String reason = "고객 요청";
            OrderResponse cancelledOrder = OrderResponse.builder()
                    .id(1L)
                    .orderItems(testOrderResponse.getOrderItems())
                    .totalAmount(testOrderResponse.getTotalAmount())
                    .status(OrderStatus.CANCELLED)
                    .orderDate(testOrderResponse.getOrderDate())
                    .build();

            given(orderService.cancelOrder(eq(1L), anyString())).willReturn(cancelledOrder);

            // When & Then
            mockMvc.perform(patch("/api/orders/1/cancel")
                            .param("reason", reason))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("CANCELLED"));

            verify(orderService).cancelOrder(eq(1L), anyString());
        }

        @Test
        @DisplayName("이미 배송 완료된 주문 취소 시도 - 400 에러")
        void cancelOrder_배송완료주문_400에러() throws Exception {
            // Given
            given(orderService.cancelOrder(eq(1L), anyString()))
                    .willThrow(new OrderException.InvalidOrderStateException("이미 배송 완료된 주문은 취소할 수 없습니다."));

            // When & Then
            mockMvc.perform(patch("/api/orders/1/cancel"))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(orderService).cancelOrder(eq(1L), anyString());
        }
    }

    @Nested
    @DisplayName("주문 검색")
    class OrderSearchTest {

        @Test
        @DisplayName("상태별 주문 조회 성공")
        void getOrdersByStatus_유효한상태_조회성공() throws Exception {
            // Given
            List<OrderResponse> pendingOrders = List.of(testOrderResponse);
            given(orderService.findOrdersByStatus(OrderStatus.PENDING)).willReturn(pendingOrders);

            // When & Then
            mockMvc.perform(get("/api/orders/status/PENDING"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].status").value("PENDING"));

            verify(orderService).findOrdersByStatus(OrderStatus.PENDING);
        }

        @Test
        @DisplayName("특정 도서가 포함된 주문 조회 성공")
        void getOrdersByBook_유효한도서ID_조회성공() throws Exception {
            // Given
            List<OrderResponse> orders = List.of(testOrderResponse);
            given(orderService.findOrdersByBookId(1L)).willReturn(orders);

            // When & Then
            mockMvc.perform(get("/api/orders/book/1"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].id").value(1L));

            verify(orderService).findOrdersByBookId(1L);
        }
    }

    @Nested
    @DisplayName("주문 통계")
    class OrderStatisticsTest {

        @Test
        @DisplayName("주문 통계 조회 성공")
        void getOrderStatistics_조회성공() throws Exception {
            // Given
            given(orderService.getTotalOrdersCount()).willReturn(100L);
            given(orderService.getOrdersCountByStatus(OrderStatus.PENDING)).willReturn(20L);
            given(orderService.getOrdersCountByStatus(OrderStatus.CONFIRMED)).willReturn(30L);
            given(orderService.getOrdersCountByStatus(OrderStatus.SHIPPED)).willReturn(25L);
            given(orderService.getOrdersCountByStatus(OrderStatus.DELIVERED)).willReturn(20L);
            given(orderService.getOrdersCountByStatus(OrderStatus.CANCELLED)).willReturn(5L);
            given(orderService.getTotalRevenue()).willReturn(new BigDecimal("10000000"));

            // When & Then
            mockMvc.perform(get("/api/orders/statistics"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalOrders").value(100))
                    .andExpect(jsonPath("$.pendingOrders").value(20))
                    .andExpect(jsonPath("$.confirmedOrders").value(30))
                    .andExpect(jsonPath("$.totalRevenue").value(10000000));

            verify(orderService).getTotalOrdersCount();
            verify(orderService).getTotalRevenue();
        }
    }
}