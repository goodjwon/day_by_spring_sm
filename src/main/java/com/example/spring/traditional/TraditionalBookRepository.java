package com.example.spring.traditional;

import com.example.spring.domain.vo.ISBN;
import com.example.spring.domain.vo.Money;
import com.example.spring.domain.model.Book;
import com.example.spring.exception.ErrorMessages;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// 1. 데이터 액세스 레이어 - 강한 결합과 중복 코드
public class TraditionalBookRepository {
    private static final String DB_URL = "jdbc:h2:mem:traditionaldb;DB_CLOSE_DELAY=-1";
    private Connection connection;

    public TraditionalBookRepository() {
        try {
            // 하드코딩된 DB 연결 정보 - 설정 변경이 어려움
            this.connection = DriverManager.getConnection(
                    DB_URL,
                    "sa",
                    ""
            );
        } catch (SQLException e) {
            throw new RuntimeException(ErrorMessages.TRADITIONAL_DB_CONNECT_FAILED, e);
        }
    }

    public Book findById(Long id) {
        // 반복적인 JDBC 코드 (boilerplate)
        String sql = "SELECT * FROM book WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Book.builder()
                        .id(rs.getLong("id"))
                        .title(rs.getString("title"))
                        .author(rs.getString("author"))
                        .isbn(ISBN.of(rs.getString("isbn")))
                        .price(Money.of(rs.getBigDecimal("price")))
                        .build();
            }
        } catch (SQLException e) {
            throw new RuntimeException(ErrorMessages.TRADITIONAL_BOOK_QUERY_FAILED, e);
        }
        return null;
    }

    public List<Book> findAll() {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT * FROM book";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                books.add(Book.builder()
                        .id(rs.getLong("id"))
                        .title(rs.getString("title"))
                        .author(rs.getString("author"))
                        .isbn(ISBN.of(rs.getString("isbn")))
                        .price(Money.of(rs.getBigDecimal("price")))
                        .build());
            }
        } catch (SQLException e) {
            throw new RuntimeException(ErrorMessages.TRADITIONAL_BOOK_LIST_QUERY_FAILED, e);
        }
        return books;
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            throw new RuntimeException(ErrorMessages.TRADITIONAL_DB_CLOSE_FAILED, e);
        }
    }
}