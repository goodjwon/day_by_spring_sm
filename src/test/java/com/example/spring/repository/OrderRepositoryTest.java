package com.example.spring.repository;

import com.example.spring.domain.vo.ISBN;
import com.example.spring.domain.vo.Money;
import com.example.spring.entity.*;
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
@DisplayName("OrderRepository 테스트")
public class OrderRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private OrderRepository orderRepository;

    private Member createAndSaveMember(String name, String email) {
        Member member = Member.builder()
                .name(name)
                .email(email)
                .membershipType(MembershipType.REGULAR)
                .joinDate(LocalDateTime.now())
                .build();
        return entityManager.persistAndFlush(member);
    }

    private static long isbnCounter = 0;

    private Book createAndSaveBook(String title, String author, BigDecimal price) {
        Book book = Book.builder()
                .title(title)
                .author(author)
                .price(Money.of(price))
                .isbn(ISBN.of("ISBN" + (System.nanoTime() % 1000000000L) + (++isbnCounter)))
                .available(true)
                .createdDate(LocalDateTime.now())
                .build();
        return entityManager.persistAndFlush(book);
    }

    @Test
    public void save_신규주문_저장성공() {
        // Given
        Member member = createAndSaveMember("Tester", "test@test.com");
        Order newOrder = Order.builder()
                .member(member)
                .totalAmount(Money.of(new BigDecimal("10000")))
                .orderDate(LocalDateTime.now())
                .build();

        // When
        Order savedOrder = orderRepository.save(newOrder);

        // Then
        assertThat(savedOrder.getId()).isNotNull();
        assertThat(savedOrder.getTotalAmount()).isEqualTo(new BigDecimal("10000"));
        assertThat(savedOrder.getOrderDate()).isNotNull();
        assertThat(savedOrder.getMember()).isEqualTo(member);
    }

    @Test
    public void save_주문아이템포함_저장성공() {
        // Given
        Member member = createAndSaveMember("Tester", "test@test.com");
        Book book = createAndSaveBook("Clean Code", "Robert C. Martin", new BigDecimal("38000"));

        Order order = Order.builder()
                .member(member)
                .totalAmount(Money.of(new BigDecimal("10000")))
                .orderDate(LocalDateTime.now())
                .build();

        OrderItem orderItem = OrderItem.builder()
                .book(book)
                .quantity(1)
                .price(book.getPrice())
                .build();

        order.addOrderItem(orderItem);

        // When
        Order savedOrder = orderRepository.save(order);
        entityManager.flush();
        entityManager.clear();

        // Then
        Order foundOrder = entityManager.find(Order.class, savedOrder.getId());
        assertThat(foundOrder).isNotNull();
        assertThat(foundOrder.getOrderItems()).hasSize(1);
        assertThat(foundOrder.getOrderItems().get(0).getBook().getTitle()).isEqualTo("Clean Code");
    }

    @Test
    public void findById_존재하는주문_주문반환() {
        // Given
        Member member = createAndSaveMember("Tester", "test@test.com");
        Order savedOrder = entityManager.persist(
                Order.builder()
                        .member(member)
                        .totalAmount(Money.of(new BigDecimal("10000")))
                        .orderDate(LocalDateTime.now())
                        .build()
        );
        entityManager.flush();

        // When
        Optional<Order> foundOrder = orderRepository.findById(savedOrder.getId());

        // Then
        assertThat(foundOrder).isPresent();
        assertThat(foundOrder.get().getTotalAmount()).isEqualToComparingFieldByField(new BigDecimal("10000"));
    }

    @Test
    public void findById_존재하지않는주문_빈Optional반환() {
        // When
        Optional<Order> foundOrder = orderRepository.findById(999L);

        // Then
        assertThat(foundOrder).isEmpty();
    }

    @Test
    public void findAll_주문목록반환() {
        // Given
        Member member = createAndSaveMember("Tester", "test@test.com");
        entityManager.persist(
                Order.builder()
                        .member(member)
                        .totalAmount(Money.of(new BigDecimal("10000")))
                        .orderDate(LocalDateTime.now())
                        .build()
        );
        entityManager.persist(
                Order.builder()
                        .member(member)
                        .totalAmount(Money.of(new BigDecimal("20000")))
                        .orderDate(LocalDateTime.now())
                        .build()
        );
        entityManager.flush();

        // When
        List<Order> orders = orderRepository.findAll();

        // Then
        assertThat(orders).isNotEmpty();
        assertThat(orders).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    public void findOrderById_존재하는주문_주문직접반환() {
        // Given
        Member member = createAndSaveMember("Tester", "test@test.com");
        Order savedOrder = entityManager.persist(
                Order.builder()
                        .member(member)
                        .totalAmount(Money.of(new BigDecimal("10000")))
                        .orderDate(LocalDateTime.now())
                        .build()
        );
        entityManager.flush();

        // When
        Order foundOrder = orderRepository.findOrderById(savedOrder.getId());

        // Then
        assertThat(foundOrder).isNotNull();
        assertThat(foundOrder.getTotalAmount()).isEqualToComparingFieldByField(new BigDecimal("10000"));
    }

    @Test
    public void findOrderById_존재하지않는주문_null반환() {
        // When
        Order foundOrder = orderRepository.findOrderById(999L);

        // Then
        assertThat(foundOrder).isNull();
    }

    // ========== 상태별 조회 테스트 ==========

    @Nested
    @DisplayName("findByStatus 테스트")
    class FindByStatusTest {

        @Test
        @DisplayName("PENDING 상태 주문 조회")
        public void findByStatus_PENDING_해당주문반환() {
            // Given
            Member member = createAndSaveMember("Tester", "status@test.com");

            Order pendingOrder = Order.builder()
                    .member(member)
                    .totalAmount(Money.of(new BigDecimal("10000")))
                    .orderDate(LocalDateTime.now())
                    .status(OrderStatus.PENDING)
                    .build();
            entityManager.persist(pendingOrder);

            Order confirmedOrder = Order.builder()
                    .member(member)
                    .totalAmount(Money.of(new BigDecimal("20000")))
                    .orderDate(LocalDateTime.now())
                    .status(OrderStatus.CONFIRMED)
                    .build();
            entityManager.persist(confirmedOrder);
            entityManager.flush();

            // When
            List<Order> pendingOrders = orderRepository.findByStatus(OrderStatus.PENDING);

            // Then
            assertThat(pendingOrders).hasSize(1);
            assertThat(pendingOrders.get(0).getStatus()).isEqualTo(OrderStatus.PENDING);
        }

        @Test
        @DisplayName("SHIPPED 상태 주문 조회")
        public void findByStatus_SHIPPED_해당주문반환() {
            // Given
            Member member = createAndSaveMember("Tester", "shipped@test.com");

            Order shippedOrder1 = Order.builder()
                    .member(member)
                    .totalAmount(Money.of(new BigDecimal("15000")))
                    .orderDate(LocalDateTime.now())
                    .status(OrderStatus.SHIPPED)
                    .build();
            entityManager.persist(shippedOrder1);

            Order shippedOrder2 = Order.builder()
                    .member(member)
                    .totalAmount(Money.of(new BigDecimal("25000")))
                    .orderDate(LocalDateTime.now())
                    .status(OrderStatus.SHIPPED)
                    .build();
            entityManager.persist(shippedOrder2);
            entityManager.flush();

            // When
            List<Order> shippedOrders = orderRepository.findByStatus(OrderStatus.SHIPPED);

            // Then
            assertThat(shippedOrders).hasSize(2);
            assertThat(shippedOrders).allMatch(order -> order.getStatus() == OrderStatus.SHIPPED);
        }

        @Test
        @DisplayName("해당 상태 주문 없으면 빈 리스트 반환")
        public void findByStatus_없는상태_빈리스트반환() {
            // Given
            Member member = createAndSaveMember("Tester", "empty@test.com");

            Order pendingOrder = Order.builder()
                    .member(member)
                    .totalAmount(Money.of(new BigDecimal("10000")))
                    .orderDate(LocalDateTime.now())
                    .status(OrderStatus.PENDING)
                    .build();
            entityManager.persist(pendingOrder);
            entityManager.flush();

            // When
            List<Order> cancelledOrders = orderRepository.findByStatus(OrderStatus.CANCELLED);

            // Then
            assertThat(cancelledOrders).isEmpty();
        }
    }

    @Nested
    @DisplayName("countByStatus 테스트")
    class CountByStatusTest {

        @Test
        @DisplayName("상태별 주문 개수 조회")
        public void countByStatus_상태별개수반환() {
            // Given
            Member member = createAndSaveMember("Tester", "count@test.com");

            for (int i = 0; i < 3; i++) {
                Order order = Order.builder()
                        .member(member)
                        .totalAmount(Money.of(new BigDecimal("10000")))
                        .orderDate(LocalDateTime.now())
                        .status(OrderStatus.PENDING)
                        .build();
                entityManager.persist(order);
            }

            for (int i = 0; i < 2; i++) {
                Order order = Order.builder()
                        .member(member)
                        .totalAmount(Money.of(new BigDecimal("20000")))
                        .orderDate(LocalDateTime.now())
                        .status(OrderStatus.DELIVERED)
                        .build();
                entityManager.persist(order);
            }
            entityManager.flush();

            // When
            long pendingCount = orderRepository.countByStatus(OrderStatus.PENDING);
            long deliveredCount = orderRepository.countByStatus(OrderStatus.DELIVERED);
            long cancelledCount = orderRepository.countByStatus(OrderStatus.CANCELLED);

            // Then
            assertThat(pendingCount).isEqualTo(3);
            assertThat(deliveredCount).isEqualTo(2);
            assertThat(cancelledCount).isEqualTo(0);
        }
    }

    // ========== 날짜 범위 조회 테스트 ==========

    @Nested
    @DisplayName("findByOrderDateBetween 테스트")
    class FindByOrderDateBetweenTest {

        @Test
        @DisplayName("날짜 범위 내 주문 조회")
        public void findByOrderDateBetween_범위내주문_조회성공() {
            // Given
            Member member = createAndSaveMember("Tester", "date@test.com");

            LocalDateTime now = LocalDateTime.now();
            LocalDateTime threeDaysAgo = now.minusDays(3);
            LocalDateTime fiveDaysAgo = now.minusDays(5);
            LocalDateTime tenDaysAgo = now.minusDays(10);

            // 10일 전 주문
            Order oldOrder = Order.builder()
                    .member(member)
                    .totalAmount(Money.of(new BigDecimal("10000")))
                    .orderDate(tenDaysAgo)
                    .status(OrderStatus.DELIVERED)
                    .build();
            entityManager.persist(oldOrder);

            // 5일 전 주문
            Order midOrder = Order.builder()
                    .member(member)
                    .totalAmount(Money.of(new BigDecimal("20000")))
                    .orderDate(fiveDaysAgo)
                    .status(OrderStatus.SHIPPED)
                    .build();
            entityManager.persist(midOrder);

            // 오늘 주문
            Order recentOrder = Order.builder()
                    .member(member)
                    .totalAmount(Money.of(new BigDecimal("30000")))
                    .orderDate(now)
                    .status(OrderStatus.PENDING)
                    .build();
            entityManager.persist(recentOrder);
            entityManager.flush();

            // When - 7일 전부터 오늘까지
            LocalDateTime startDate = now.minusDays(7);
            LocalDateTime endDate = now.plusDays(1);
            List<Order> orders = orderRepository.findByOrderDateBetween(startDate, endDate);

            // Then - 5일 전, 오늘 주문만 조회됨 (10일 전은 제외)
            assertThat(orders).hasSize(2);
            assertThat(orders).extracting("totalAmount")
                    .containsExactlyInAnyOrder(new BigDecimal("20000"), new BigDecimal("30000"));
        }

        @Test
        @DisplayName("범위 밖 주문은 조회되지 않음")
        public void findByOrderDateBetween_범위밖주문_조회안됨() {
            // Given
            Member member = createAndSaveMember("Tester", "dateout@test.com");

            LocalDateTime tenDaysAgo = LocalDateTime.now().minusDays(10);
            Order oldOrder = Order.builder()
                    .member(member)
                    .totalAmount(Money.of(new BigDecimal("10000")))
                    .orderDate(tenDaysAgo)
                    .status(OrderStatus.DELIVERED)
                    .build();
            entityManager.persist(oldOrder);
            entityManager.flush();

            // When - 3일 전부터 오늘까지
            LocalDateTime startDate = LocalDateTime.now().minusDays(3);
            LocalDateTime endDate = LocalDateTime.now();
            List<Order> orders = orderRepository.findByOrderDateBetween(startDate, endDate);

            // Then
            assertThat(orders).isEmpty();
        }
    }

    // ========== 금액 범위 조회 테스트 ==========

    @Nested
    @DisplayName("findByTotalAmountBetween 테스트")
    class FindByTotalAmountBetweenTest {

        @Test
        @DisplayName("금액 범위 내 주문 조회")
        public void findByTotalAmountBetween_범위내주문_조회성공() {
            // Given
            Member member = createAndSaveMember("Tester", "amount@test.com");

            Order cheapOrder = Order.builder()
                    .member(member)
                    .totalAmount(Money.of(new BigDecimal("5000")))
                    .orderDate(LocalDateTime.now())
                    .status(OrderStatus.PENDING)
                    .build();
            entityManager.persist(cheapOrder);

            Order midOrder = Order.builder()
                    .member(member)
                    .totalAmount(Money.of(new BigDecimal("50000")))
                    .orderDate(LocalDateTime.now())
                    .status(OrderStatus.PENDING)
                    .build();
            entityManager.persist(midOrder);

            Order expensiveOrder = Order.builder()
                    .member(member)
                    .totalAmount(Money.of(new BigDecimal("200000")))
                    .orderDate(LocalDateTime.now())
                    .status(OrderStatus.PENDING)
                    .build();
            entityManager.persist(expensiveOrder);
            entityManager.flush();

            // When - 10,000원 ~ 100,000원 사이
            List<Order> orders = orderRepository.findByTotalAmountBetween(
                    new BigDecimal("10000"), new BigDecimal("100000"));

            // Then - 50,000원 주문만 조회
            assertThat(orders).hasSize(1);
            assertThat(orders.get(0).getTotalAmount()).isEqualToComparingFieldByField(new BigDecimal("50000"));
        }

        @Test
        @DisplayName("경계값 포함 확인")
        public void findByTotalAmountBetween_경계값포함_조회성공() {
            // Given
            Member member = createAndSaveMember("Tester", "boundary@test.com");

            Order exactMinOrder = Order.builder()
                    .member(member)
                    .totalAmount(Money.of(new BigDecimal("10000")))
                    .orderDate(LocalDateTime.now())
                    .status(OrderStatus.PENDING)
                    .build();
            entityManager.persist(exactMinOrder);

            Order exactMaxOrder = Order.builder()
                    .member(member)
                    .totalAmount(Money.of(new BigDecimal("50000")))
                    .orderDate(LocalDateTime.now())
                    .status(OrderStatus.PENDING)
                    .build();
            entityManager.persist(exactMaxOrder);
            entityManager.flush();

            // When
            List<Order> orders = orderRepository.findByTotalAmountBetween(
                    new BigDecimal("10000"), new BigDecimal("50000"));

            // Then - 경계값 포함되어야 함
            assertThat(orders).hasSize(2);
        }

        @Test
        @DisplayName("범위에 주문 없으면 빈 리스트")
        public void findByTotalAmountBetween_범위없음_빈리스트() {
            // Given
            Member member = createAndSaveMember("Tester", "norange@test.com");

            Order order = Order.builder()
                    .member(member)
                    .totalAmount(Money.of(new BigDecimal("5000")))
                    .orderDate(LocalDateTime.now())
                    .status(OrderStatus.PENDING)
                    .build();
            entityManager.persist(order);
            entityManager.flush();

            // When
            List<Order> orders = orderRepository.findByTotalAmountBetween(
                    new BigDecimal("100000"), new BigDecimal("200000"));

            // Then
            assertThat(orders).isEmpty();
        }
    }

    // ========== 도서 ID로 주문 조회 테스트 ==========

    @Nested
    @DisplayName("findByOrderItems_Book_Id 테스트")
    class FindByOrderItemsBookIdTest {

        @Test
        @DisplayName("특정 도서가 포함된 주문 조회")
        public void findByOrderItems_Book_Id_도서포함주문_조회성공() {
            // Given
            Member member = createAndSaveMember("Tester", "book@test.com");
            Book targetBook = createAndSaveBook("Target Book", "Author", new BigDecimal("30000"));
            Book otherBook = createAndSaveBook("Other Book", "Author2", new BigDecimal("20000"));

            // targetBook이 포함된 주문 1
            Order order1 = Order.builder()
                    .member(member)
                    .totalAmount(Money.of(new BigDecimal("30000")))
                    .orderDate(LocalDateTime.now())
                    .status(OrderStatus.PENDING)
                    .build();
            OrderItem item1 = OrderItem.builder()
                    .book(targetBook)
                    .quantity(1)
                    .price(targetBook.getPrice())
                    .build();
            order1.addOrderItem(item1);
            entityManager.persist(order1);

            // targetBook이 포함된 주문 2
            Order order2 = Order.builder()
                    .member(member)
                    .totalAmount(Money.of(new BigDecimal("60000")))
                    .orderDate(LocalDateTime.now())
                    .status(OrderStatus.CONFIRMED)
                    .build();
            OrderItem item2 = OrderItem.builder()
                    .book(targetBook)
                    .quantity(2)
                    .price(targetBook.getPrice())
                    .build();
            order2.addOrderItem(item2);
            entityManager.persist(order2);

            // otherBook만 포함된 주문
            Order order3 = Order.builder()
                    .member(member)
                    .totalAmount(Money.of(new BigDecimal("20000")))
                    .orderDate(LocalDateTime.now())
                    .status(OrderStatus.PENDING)
                    .build();
            OrderItem item3 = OrderItem.builder()
                    .book(otherBook)
                    .quantity(1)
                    .price(otherBook.getPrice())
                    .build();
            order3.addOrderItem(item3);
            entityManager.persist(order3);

            entityManager.flush();

            // When
            List<Order> ordersWithTargetBook = orderRepository.findByOrderItems_Book_Id(targetBook.getId());

            // Then - targetBook이 포함된 주문 2개만 조회
            assertThat(ordersWithTargetBook).hasSize(2);
            assertThat(ordersWithTargetBook).extracting("totalAmount")
                    .containsExactlyInAnyOrder(new BigDecimal("30000"), new BigDecimal("60000"));
        }

        @Test
        @DisplayName("도서가 포함된 주문 없으면 빈 리스트")
        public void findByOrderItems_Book_Id_없음_빈리스트() {
            // Given
            Member member = createAndSaveMember("Tester", "nobook@test.com");
            Book book = createAndSaveBook("Lonely Book", "Author", new BigDecimal("10000"));

            // 다른 도서만 포함된 주문
            Book otherBook = createAndSaveBook("Other", "Other", new BigDecimal("5000"));
            Order order = Order.builder()
                    .member(member)
                    .totalAmount(Money.of(new BigDecimal("5000")))
                    .orderDate(LocalDateTime.now())
                    .status(OrderStatus.PENDING)
                    .build();
            OrderItem item = OrderItem.builder()
                    .book(otherBook)
                    .quantity(1)
                    .price(otherBook.getPrice())
                    .build();
            order.addOrderItem(item);
            entityManager.persist(order);
            entityManager.flush();

            // When
            List<Order> orders = orderRepository.findByOrderItems_Book_Id(book.getId());

            // Then
            assertThat(orders).isEmpty();
        }

        @Test
        @DisplayName("하나의 주문에 여러 도서가 있을 때 특정 도서로 검색")
        public void findByOrderItems_Book_Id_여러도서중하나_조회성공() {
            // Given
            Member member = createAndSaveMember("Tester", "multi@test.com");
            Book book1 = createAndSaveBook("Book 1", "Author1", new BigDecimal("10000"));
            Book book2 = createAndSaveBook("Book 2", "Author2", new BigDecimal("20000"));
            Book book3 = createAndSaveBook("Book 3", "Author3", new BigDecimal("30000"));

            // 3권이 포함된 주문
            Order order = Order.builder()
                    .member(member)
                    .totalAmount(Money.of(new BigDecimal("60000")))
                    .orderDate(LocalDateTime.now())
                    .status(OrderStatus.PENDING)
                    .build();

            order.addOrderItem(OrderItem.builder().book(book1).quantity(1).price(book1.getPrice()).build());
            order.addOrderItem(OrderItem.builder().book(book2).quantity(1).price(book2.getPrice()).build());
            order.addOrderItem(OrderItem.builder().book(book3).quantity(1).price(book3.getPrice()).build());
            entityManager.persist(order);
            entityManager.flush();

            // When - book2로 검색
            List<Order> orders = orderRepository.findByOrderItems_Book_Id(book2.getId());

            // Then
            assertThat(orders).hasSize(1);
            assertThat(orders.get(0).getOrderItems()).hasSize(3);
        }
    }

    // ========== 삭제 테스트 ==========

    @Nested
    @DisplayName("delete 테스트")
    class DeleteTest {

        @Test
        @DisplayName("주문 삭제 성공")
        public void delete_주문삭제_성공() {
            // Given
            Member member = createAndSaveMember("Tester", "delete@test.com");
            Order order = Order.builder()
                    .member(member)
                    .totalAmount(Money.of(new BigDecimal("10000")))
                    .orderDate(LocalDateTime.now())
                    .status(OrderStatus.CANCELLED)
                    .build();
            Order savedOrder = entityManager.persistAndFlush(order);
            Long orderId = savedOrder.getId();

            // When
            orderRepository.deleteById(orderId);
            entityManager.flush();

            // Then
            Order foundOrder = entityManager.find(Order.class, orderId);
            assertThat(foundOrder).isNull();
        }

        @Test
        @DisplayName("주문 삭제 시 주문 항목도 함께 삭제 (Cascade)")
        public void delete_주문삭제시_항목도삭제() {
            // Given
            Member member = createAndSaveMember("Tester", "cascade@test.com");
            Book book = createAndSaveBook("To Delete", "Author", new BigDecimal("10000"));

            Order order = Order.builder()
                    .member(member)
                    .totalAmount(Money.of(new BigDecimal("10000")))
                    .orderDate(LocalDateTime.now())
                    .status(OrderStatus.CANCELLED)
                    .build();

            OrderItem item = OrderItem.builder()
                    .book(book)
                    .quantity(1)
                    .price(book.getPrice())
                    .build();
            order.addOrderItem(item);

            Order savedOrder = entityManager.persistAndFlush(order);
            Long orderId = savedOrder.getId();
            Long itemId = savedOrder.getOrderItems().get(0).getId();

            entityManager.clear();

            // When
            orderRepository.deleteById(orderId);
            entityManager.flush();

            // Then
            assertThat(entityManager.find(Order.class, orderId)).isNull();
            assertThat(entityManager.find(OrderItem.class, itemId)).isNull();
        }
    }

}