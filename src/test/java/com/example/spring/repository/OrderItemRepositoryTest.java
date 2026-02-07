package com.example.spring.repository;

import com.example.spring.domain.vo.ISBN;
import com.example.spring.domain.vo.Money;
import com.example.spring.entity.*;
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
public class OrderItemRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private OrderItemRepository orderItemRepository;

    private Member createAndSaveMember(String name, String email) {
        Member member = Member.builder()
                .name(name)
                .email(email)
                .membershipType(MembershipType.REGULAR)
                .joinDate(LocalDateTime.now())
                .build();
        return entityManager.persistAndFlush(member);
    }

    private Book createAndSaveBook(String title, String author, BigDecimal price) {
        Book book = Book.builder()
                .title(title)
                .author(author)
                .price(Money.of(price))
                .isbn(ISBN.of("ISBN" + (System.nanoTime() % 1000000000L)))
                .available(true)
                .createdDate(LocalDateTime.now())
                .build();
        return entityManager.persistAndFlush(book);
    }

    private Order createAndSaveOrder(Member member) {
        Order order = Order.builder()
                .member(member)
                .totalAmount(Money.of(new BigDecimal("10000")))
                .orderDate(LocalDateTime.now())
                .build();
        return entityManager.persistAndFlush(order);
    }

    @Test
    public void save_신규주문상품_저장성공() {
        // Given
        Member member = createAndSaveMember("Tester", "test@test.com");
        Book book = createAndSaveBook("Clean Code", "Robert C. Martin", new BigDecimal("38000"));
        Order order = createAndSaveOrder(member);

        OrderItem orderItem = OrderItem.builder()
                .order(order)
                .book(book)
                .quantity(2)
                .price(book.getPrice())
                .build();

        // When
        OrderItem savedItem = orderItemRepository.save(orderItem);

        // Then
        assertThat(savedItem.getId()).isNotNull();
        assertThat(savedItem.getBook()).isEqualTo(book);
        assertThat(savedItem.getOrder()).isEqualTo(order);
        assertThat(savedItem.getQuantity()).isEqualTo(2);
    }

    @Test
    public void findById_존재하는주문상품_주문상품반환() {
        // Given
        Member member = createAndSaveMember("Tester", "test@test.com");
        Book book = createAndSaveBook("Clean Code", "Robert C. Martin", new BigDecimal("38000"));
        Order order = createAndSaveOrder(member);

        OrderItem orderItem = OrderItem.builder()
                .order(order)
                .book(book)
                .quantity(1)
                .price(book.getPrice())
                .build();
        OrderItem savedItem = entityManager.persistAndFlush(orderItem);

        // When
        Optional<OrderItem> foundItem = orderItemRepository.findById(savedItem.getId());

        // Then
        assertThat(foundItem).isPresent();
        assertThat(foundItem.get().getBook().getTitle()).isEqualTo("Clean Code");
    }

    @Test
    public void findById_존재하지않는주문상품_빈Optional반환() {
        // When
        Optional<OrderItem> foundItem = orderItemRepository.findById(999L);

        // Then
        assertThat(foundItem).isEmpty();
    }

    @Test
    public void findByOrderId_주문별상품조회() {
        // Given
        Member member = createAndSaveMember("Tester", "test@test.com");
        Book book1 = createAndSaveBook("Book 1", "Author 1", new BigDecimal("10000"));
        Book book2 = createAndSaveBook("Book 2", "Author 2", new BigDecimal("20000"));
        Order order = createAndSaveOrder(member);

        OrderItem item1 = OrderItem.builder().order(order).book(book1).quantity(1).price(book1.getPrice()).build();
        OrderItem item2 = OrderItem.builder().order(order).book(book2).quantity(2).price(book2.getPrice()).build();

        entityManager.persist(item1);
        entityManager.persist(item2);
        entityManager.flush();

        // When
        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());

        // Then
        assertThat(items).hasSize(2);
    }

    @Test
    public void findByBookId_도서별주문내역조회() {
        // Given
        Member member = createAndSaveMember("Tester", "test@test.com");
        Book book = createAndSaveBook("Best Seller", "Famous Author", new BigDecimal("15000"));
        Order order1 = createAndSaveOrder(member);
        Order order2 = createAndSaveOrder(member);

        OrderItem item1 = OrderItem.builder().order(order1).book(book).quantity(1).price(book.getPrice()).build();
        OrderItem item2 = OrderItem.builder().order(order2).book(book).quantity(1).price(book.getPrice()).build();

        entityManager.persist(item1);
        entityManager.persist(item2);
        entityManager.flush();

        // When
        List<OrderItem> items = orderItemRepository.findByBookId(book.getId());

        // Then
        assertThat(items).hasSize(2);
    }

    @Test
    public void findByOrderIdAndBookId_특정주문의특정도서조회() {
        // Given
        Member member = createAndSaveMember("Tester", "test@test.com");
        Book book = createAndSaveBook("Target Book", "Target Author", new BigDecimal("12000"));
        Order order = createAndSaveOrder(member);

        OrderItem item = OrderItem.builder().order(order).book(book).quantity(3).price(book.getPrice()).build();
        entityManager.persistAndFlush(item);

        // When
        List<OrderItem> foundItems = orderItemRepository.findByOrderIdAndBookId(order.getId(), book.getId());

        // Then
        assertThat(foundItems).isNotEmpty();
        assertThat(foundItems.get(0).getQuantity()).isEqualTo(3);
    }

    @Test
    public void findAll_전체주문상품조회() {
        // Given
        Member member = createAndSaveMember("Tester", "test@test.com");
        Book book = createAndSaveBook("Any Book", "Any Author", new BigDecimal("10000"));
        Order order = createAndSaveOrder(member);

        entityManager.persist(OrderItem.builder().order(order).book(book).quantity(1).price(book.getPrice()).build());
        entityManager.persist(OrderItem.builder().order(order).book(book).quantity(1).price(book.getPrice()).build());
        entityManager.flush();

        // When
        List<OrderItem> items = orderItemRepository.findAll();

        // Then
        assertThat(items).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    public void deleteById_주문상품삭제() {
        // Given
        Member member = createAndSaveMember("Tester", "test@test.com");
        Book book = createAndSaveBook("To be deleted", "Author", new BigDecimal("10000"));
        Order order = createAndSaveOrder(member);

        OrderItem item = OrderItem.builder().order(order).book(book).quantity(1).price(book.getPrice()).build();
        OrderItem savedItem = entityManager.persistAndFlush(item);

        // When
        orderItemRepository.deleteById(savedItem.getId());
        entityManager.flush();

        // Then
        OrderItem foundItem = entityManager.find(OrderItem.class, savedItem.getId());
        assertThat(foundItem).isNull();
    }

    @Test
    public void findOrderItemById_편의메서드_주문상품직접반환() {
        // Given
        Member member = createAndSaveMember("Tester", "test@test.com");
        Book book = createAndSaveBook("Convenience Test", "Author", new BigDecimal("10000"));
        Order order = createAndSaveOrder(member);

        OrderItem item = OrderItem.builder().order(order).book(book).quantity(1).price(book.getPrice()).build();
        OrderItem savedItem = entityManager.persistAndFlush(item);

        // When
        OrderItem foundItem = orderItemRepository.findOrderItemById(savedItem.getId());

        // Then
        assertThat(foundItem).isNotNull();
        assertThat(foundItem.getId()).isEqualTo(savedItem.getId());
    }

    @Test
    public void findOrderItemById_존재하지않는ID_null반환() {
        // When
        OrderItem foundItem = orderItemRepository.findOrderItemById(999L);

        // Then
        assertThat(foundItem).isNull();
    }
}