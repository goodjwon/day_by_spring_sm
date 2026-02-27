package com.example.spring.util;

import com.example.spring.domain.vo.Address;
import com.example.spring.domain.vo.ISBN;
import com.example.spring.domain.vo.Money;
import com.example.spring.domain.model.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 테스트용 데이터 빌더 클래스
 */
public class TestDataBuilder {

    public static Book createBook(String title, String author, String isbn, BigDecimal price) {
        return Book.builder()
                .title(title)
                .author(author)
                .isbn(ISBN.of(isbn))
                .price(Money.of(price))
                .available(true)
                .createdDate(LocalDateTime.now())
                .build();
    }

    public static Book createBook(Long id, String title, String author, String isbn, BigDecimal price) {
        return Book.builder()
                .id(id)
                .title(title)
                .author(author)
                .isbn(ISBN.of(isbn))
                .price(Money.of(price))
                .available(true)
                .createdDate(LocalDateTime.now())
                .build();
    }

    public static Book createBookWithId(Long id) {
        return Book.builder()
                .id(id)
                .title("Test Book " + id)
                .author("Test Author")
                .isbn(ISBN.of("978123456789" + (id % 10)))
                .price(Money.of(new BigDecimal("25000")))
                .available(true)
                .createdDate(LocalDateTime.now())
                .build();
    }

    public static Order createOrder(Member member, Money totalAmount) {
        return Order.builder()
                .member(member)
                .totalAmount(totalAmount)
                .discountAmount(Money.zero())
                .orderDate(LocalDateTime.now())
                .status(OrderStatus.PENDING)
                .build();
    }

    public static Order createOrder(Member member, BigDecimal totalAmount) {
        return createOrder(member, Money.of(totalAmount));
    }

    public static OrderItem createOrderItem(Book book, int quantity) {
        return OrderItem.builder()
                .book(book)
                .quantity(quantity)
                .price(book.getPrice())
                .build();
    }

    public static Payment createPayment(Order order, Money amount) {
        return Payment.builder()
                .order(order)
                .method(PaymentMethod.CREDIT_CARD)
                .status(PaymentStatus.PENDING)
                .amount(amount)
                .build();
    }

    public static Payment createPayment(Order order, BigDecimal amount) {
        return createPayment(order, Money.of(amount));
    }

    public static Delivery createDelivery(Order order, String recipientName,
                                          String phoneNumber, String zipCode,
                                          String address, String addressDetail) {
        return Delivery.builder()
                .order(order)
                .recipientName(recipientName)
                .phoneNumber(phoneNumber)
                .deliveryAddress(Address.of(zipCode, address, addressDetail))
                .build();
    }

    public static Refund createRefund(Order order, Money amount, String reason) {
        return Refund.builder()
                .order(order)
                .amount(amount)
                .reason(reason)
                .requestedBy("test-user")
                .build();
    }

    public static Refund createRefund(Order order, BigDecimal amount, String reason) {
        return createRefund(order, Money.of(amount), reason);
    }

    public static Loan createLoan(Member member, Book book) {
        LocalDateTime now = LocalDateTime.now();
        return Loan.builder()
                .member(member)
                .book(book)
                .loanDate(now)
                .dueDate(now.plusDays(14))
                .status(LoanStatus.ACTIVE)
                .overdueFee(Money.zero())
                .extensionCount(0)
                .build();
    }

    public static Member createMember(Long id, String email, String name) {
        return Member.builder()
                .id(id)
                .email(email)
                .name(name)
                .password("password123")
                .role(Role.USER)
                .membershipType(MembershipType.REGULAR)
                .joinDate(LocalDateTime.now())
                .build();
    }

    public static Member createMember(String email, String name) {
        return Member.builder()
                .email(email)
                .name(name)
                .password("password123")
                .role(Role.USER)
                .membershipType(MembershipType.REGULAR)
                .joinDate(LocalDateTime.now())
                .build();
    }
}