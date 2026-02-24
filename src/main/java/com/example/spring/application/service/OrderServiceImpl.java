package com.example.spring.application.service;

import com.example.spring.domain.vo.Address;
import com.example.spring.domain.vo.Money;
import com.example.spring.application.dto.request.CreateOrderRequest;
import com.example.spring.application.dto.response.OrderResponse;
import com.example.spring.domain.model.*;
import com.example.spring.domain.event.OrderCancelledEvent;
import com.example.spring.domain.event.OrderConfirmedEvent;
import com.example.spring.domain.event.OrderCreatedEvent;
import com.example.spring.exception.BookException;
import com.example.spring.exception.MemberException;
import com.example.spring.exception.OrderException;
import com.example.spring.domain.repository.*;
import com.example.spring.application.EmailService;
import com.example.spring.application.LoggingService;
import com.example.spring.application.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final DeliveryRepository deliveryRepository;
    private final EmailService emailService;
    private final LoggingService loggingService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        loggingService.log("주문 생성 시작 - 회원 ID: " + request.getMemberId());

        long startTime = System.currentTimeMillis();

        try {
            // 1. 회원 조회
            Member member = memberRepository.findById(request.getMemberId())
                    .orElseThrow(() -> new MemberException.MemberNotFoundException(request.getMemberId()));

            // 2. 주문 항목별로 도서 조회 및 총 금액 계산
            Money totalAmount = Money.zero();
            List<OrderItem> orderItems = new ArrayList<>();

            for (var itemRequest : request.getItems()) {
                Book book = bookRepository.findById(itemRequest.getBookId())
                        .orElseThrow(() -> new BookException.BookNotFoundException(itemRequest.getBookId()));

                // 재고 확인 (간단한 예시 - 실제로는 재고 관리 시스템 필요)
                if (book.isDeleted()) {
                    throw new BookException.DeletedBookAccessException("삭제된 도서는 주문할 수 없습니다: " + book.getTitle());
                }

                Money itemTotal = book.getPrice().multiply(itemRequest.getQuantity());
                totalAmount = totalAmount.add(itemTotal);

                OrderItem orderItem = OrderItem.builder()
                        .book(book)
                        .quantity(itemRequest.getQuantity())
                        .price(book.getPrice())
                        .build();
                orderItems.add(orderItem);
            }

            // 3. 주문 생성
            Money discountAmount = request.getDiscountAmount() != null
                    ? Money.of(request.getDiscountAmount())
                    : Money.zero();

            Order order = Order.builder()
                    .member(member)
                    .totalAmount(totalAmount)
                    .discountAmount(discountAmount)
                    .pointsUsed(request.getPointsUsed())
                    .couponCode(request.getCouponCode())
                    .orderDate(LocalDateTime.now())
                    .build();

            // 4. 주문 항목 추가
            for (OrderItem item : orderItems) {
                order.addOrderItem(item);
            }

            // 5. 결제 정보 생성
            Payment payment = Payment.builder()
                    .order(order)
                    .method(request.getPayment().getMethod())
                    .amount(order.getFinalAmount())
                    .pgProvider(request.getPayment().getPgProvider())
                    .cardCompany(request.getPayment().getCardCompany())
                    .cardNumber(request.getPayment().getCardNumber())
                    .installmentMonths(request.getPayment().getInstallmentMonths())
                    .build();

            // 6. 배송 정보 생성
            Address deliveryAddress = Address.of(
                    request.getDelivery().getZipCode(),
                    request.getDelivery().getAddress(),
                    request.getDelivery().getAddressDetail()
            );

            Delivery delivery = Delivery.builder()
                    .order(order)
                    .recipientName(request.getDelivery().getRecipientName())
                    .phoneNumber(request.getDelivery().getPhoneNumber())
                    .deliveryAddress(deliveryAddress)
                    .deliveryMemo(request.getDelivery().getDeliveryMemo())
                    .build();

            // 7. 연관관계 설정 (cascade로 자동 저장됨)
            order.attachPayment(payment);
            order.attachDelivery(delivery);

            // 8. 주문 저장
            Order savedOrder = orderRepository.save(order);

            // 9. 이메일 발송
            emailService.sendOrderConfirmation(savedOrder);

            // 10. 주문 생성 이벤트 발행
            eventPublisher.publishEvent(new OrderCreatedEvent(savedOrder));

            long endTime = System.currentTimeMillis();
            loggingService.log("주문 생성 완료 - 실행 시간: " + (endTime - startTime) + "ms");

            return OrderResponse.from(savedOrder);

        } catch (Exception e) {
            loggingService.error("주문 생성 실패", e);
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse findOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderException.OrderNotFoundException(id));
        return OrderResponse.from(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> findAllOrders() {
        return orderRepository.findAll().stream()
                .map(OrderResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> findAllOrdersWithPagination(Pageable pageable) {
        return orderRepository.findAll(pageable)
                .map(OrderResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> findOrdersByStatusWithPagination(OrderStatus status, Pageable pageable) {
        return orderRepository.findByStatus(status, pageable)
                .map(OrderResponse::from);
    }

    @Override
    @Transactional
    public OrderResponse confirmOrder(Long id) {
        loggingService.log("주문 확인 - 주문 ID: " + id);

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderException.OrderNotFoundException(id));

        order.confirm();
        Order updated = orderRepository.save(order);

        // 주문 확정 이벤트 발행
        eventPublisher.publishEvent(new OrderConfirmedEvent(updated));

        return OrderResponse.from(updated);
    }

    @Override
    @Transactional
    public OrderResponse shipOrder(Long id, String trackingNumber, String courierCompany) {
        loggingService.log("주문 배송 시작 - 주문 ID: " + id);

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderException.OrderNotFoundException(id));

        // 주문 상태 변경
        order.ship();

        // 배송 시작
        Delivery delivery = order.getDelivery();
        if (delivery != null) {
            delivery.startShipping(trackingNumber, courierCompany);
        }

        Order updated = orderRepository.save(order);
        return OrderResponse.from(updated);
    }

    @Override
    @Transactional
    public OrderResponse deliverOrder(Long id) {
        loggingService.log("주문 배송 완료 - 주문 ID: " + id);

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderException.OrderNotFoundException(id));

        // 주문 상태 변경
        order.deliver();

        // 배송 완료
        Delivery delivery = order.getDelivery();
        if (delivery != null) {
            delivery.complete();
        }

        Order updated = orderRepository.save(order);
        return OrderResponse.from(updated);
    }

    @Override
    @Transactional
    public OrderResponse cancelOrder(Long id, String reason) {
        loggingService.log("주문 취소 - 주문 ID: " + id);

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderException.OrderNotFoundException(id));

        order.cancel(reason);
        Order updated = orderRepository.save(order);

        // 주문 취소 이벤트 발행
        eventPublisher.publishEvent(new OrderCancelledEvent(updated, reason));

        return OrderResponse.from(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> findOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status).stream()
                .map(OrderResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> findOrdersByMemberId(Long memberId) {
        return orderRepository.findByMemberId(memberId).stream()
                .map(OrderResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> findOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return orderRepository.findByOrderDateBetween(startDate, endDate).stream()
                .map(OrderResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> findOrdersByAmountRange(BigDecimal minAmount, BigDecimal maxAmount) {
        return orderRepository.findByTotalAmountBetween(minAmount, maxAmount).stream()
                .map(OrderResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> findOrdersByBookId(Long bookId) {
        return orderRepository.findByOrderItems_Book_Id(bookId).stream()
                .map(OrderResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public long getTotalOrdersCount() {
        return orderRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public long getOrdersCountByStatus(OrderStatus status) {
        return orderRepository.countByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalRevenue() {
        return orderRepository.calculateTotalRevenue(OrderStatus.CANCELLED);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getRevenueByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return orderRepository.calculateRevenueByDateRange(startDate, endDate, OrderStatus.CANCELLED);
    }
}