package com.example.spring.traditional;

import com.example.spring.domain.model.Order;
import com.example.spring.exception.BookException;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TraditionalOrderService 테스트")
class TraditionalOrderServiceTest {

    // 데이터베이스 설정 상수
    private static final String DB_URL = "jdbc:h2:mem:traditionaldb;DB_CLOSE_DELAY=-1";
    private static final String DB_USER = "sa";
    private static final String DB_PASSWORD = "";

    // 테스트 데이터 상수
    private static final Long BOOK_ID_CLEAN_CODE = 1L;
    private static final Long BOOK_ID_SPRING_IN_ACTION = 2L;
    private static final Long NON_EXISTENT_BOOK_ID = 999L;

    private TraditionalOrderService orderService;

    @BeforeEach
    void setUp() throws Exception {
        initializeDatabase();
        insertTestBooks();
        orderService = new TraditionalOrderService();
    }

    @AfterEach
    void tearDown() throws Exception {
        cleanupOrderService();
        dropTables();
    }

    // ==================== 데이터베이스 초기화 헬퍼 메서드 ====================

    private void initializeDatabase() throws SQLException {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS book");
            stmt.execute(createBookTableSql());
        }
    }

    private void insertTestBooks() throws SQLException {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(insertBookSql(BOOK_ID_CLEAN_CODE, "Clean Code", "Robert C. Martin", "9780132350884", "45000"));
            stmt.execute(insertBookSql(BOOK_ID_SPRING_IN_ACTION, "Spring in Action", "Craig Walls", "9781617294945", "52000"));
        }
    }

    private void dropTables() throws SQLException {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS book");
        }
    }

    private void cleanupOrderService() {
        if (orderService != null) {
            orderService.cleanup();
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    private String createBookTableSql() {
        return "CREATE TABLE book (" +
                "id BIGINT PRIMARY KEY, " +
                "title VARCHAR(255), " +
                "author VARCHAR(255), " +
                "isbn VARCHAR(17), " +
                "price DECIMAL(19, 2), " +
                "available BOOLEAN" +
                ")";
    }

    private String insertBookSql(Long id, String title, String author, String isbn, String price) {
        return String.format(
                "INSERT INTO book (id, title, author, isbn, price, available) VALUES (%d, '%s', '%s', '%s', %s, true)",
                id, title, author, isbn, price
        );
    }

    // ==================== 테스트 케이스 ====================

    @Nested
    @DisplayName("정상 주문")
    class SuccessfulOrderTest {

        @Test
        @DisplayName("단일 도서 주문 성공")
        void createOrder_단일도서_성공() {
            // Given
            List<Long> bookIds = List.of(BOOK_ID_CLEAN_CODE);

            // When & Then
            assertDoesNotThrow(() -> {
                Order order = orderService.createOrder(bookIds);

                assertNotNull(order);
                assertNotNull(order.getOrderDate());
                assertNotNull(order.getTotalAmount());
                assertTrue(order.getTotalAmount().getAmount().compareTo(BigDecimal.ZERO) > 0);
            });
        }

        @Test
        @DisplayName("여러 도서 주문 성공")
        void createOrder_여러도서_성공() {
            // Given
            List<Long> bookIds = List.of(BOOK_ID_CLEAN_CODE, BOOK_ID_SPRING_IN_ACTION);

            // When & Then
            assertDoesNotThrow(() -> {
                Order order = orderService.createOrder(bookIds);

                assertNotNull(order);
                assertNotNull(order.getTotalAmount());
                assertTrue(order.getTotalAmount().getAmount().compareTo(BigDecimal.ZERO) > 0);
            });
        }

        @Test
        @DisplayName("중복 도서 주문 성공")
        void createOrder_중복도서_성공() {
            // Given
            List<Long> bookIds = List.of(BOOK_ID_CLEAN_CODE, BOOK_ID_CLEAN_CODE);

            // When & Then
            assertDoesNotThrow(() -> {
                Order order = orderService.createOrder(bookIds);

                assertNotNull(order);
                assertTrue(order.getTotalAmount().getAmount().compareTo(BigDecimal.ZERO) > 0);
            });
        }

        @Test
        @DisplayName("빈 주문 목록 - 총액 0원")
        void createOrder_빈목록_총액0원() {
            // Given
            List<Long> bookIds = Collections.emptyList();

            // When & Then
            assertDoesNotThrow(() -> {
                Order order = orderService.createOrder(bookIds);

                assertNotNull(order);
                assertEquals(0, order.getTotalAmount().getAmount().compareTo(BigDecimal.ZERO));
            });
        }
    }

    @Nested
    @DisplayName("주문 실패")
    class FailedOrderTest {

        @Test
        @DisplayName("존재하지 않는 도서 - BookNotFoundException")
        void createOrder_존재하지않는도서_예외발생() {
            // Given
            List<Long> bookIds = List.of(NON_EXISTENT_BOOK_ID);

            // When & Then
            BookException.BookNotFoundException exception = assertThrows(
                    BookException.BookNotFoundException.class,
                    () -> orderService.createOrder(bookIds)
            );

            assertTrue(exception.getMessage().contains("도서를 찾을 수 없습니다"));
        }

        @Test
        @DisplayName("혼합된 주문 목록 (존재 + 미존재) - BookNotFoundException")
        void createOrder_혼합목록_예외발생() {
            // Given
            List<Long> bookIds = List.of(BOOK_ID_CLEAN_CODE, NON_EXISTENT_BOOK_ID);

            // When & Then
            BookException.BookNotFoundException exception = assertThrows(
                    BookException.BookNotFoundException.class,
                    () -> orderService.createOrder(bookIds)
            );

            assertTrue(exception.getMessage().contains("도서를 찾을 수 없습니다"));
        }

        @Test
        @DisplayName("null 입력 - IllegalArgumentException")
        void createOrder_null입력_예외발생() {
            // Given
            List<Long> bookIds = null;

            // When & Then
            assertThrows(IllegalArgumentException.class,
                    () -> orderService.createOrder(bookIds));
        }
    }
}