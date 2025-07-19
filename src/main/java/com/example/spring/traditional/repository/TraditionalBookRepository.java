package com.example.spring.traditional.repository;

import com.example.spring.entity.Book;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

//DAO
public class TraditionalBookRepository {

    private Connection connection;
    // database 접속 :
    public TraditionalBookRepository() {
        try {
            // 하드코딩된 DB 연결 정보 - 설정 변경이 어려움
            this.connection = DriverManager.getConnection(
                    "jdbc:h2:file:./data/testdb;AUTO_SERVER=TRUE;AUTO_SERVER_PORT=9093",
                    "sa",
                    ""
            );
        } catch (SQLException e) {
            throw new RuntimeException("DB 연결 실패", e);
        }
    }

    public Book findById(Long id){
        String sql = "SELECT * FROM BOOK WHERE ID = ?";
        try(PreparedStatement stmt = connection.prepareStatement(sql)){
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            if(rs.next()){
                return Book.builder()
                        .id(rs.getLong("id"))
                        .title(rs.getString("title"))
                        .author(rs.getString("author"))
                        .price(rs.getBigDecimal("price"))
                        .build();
            }
        } catch (SQLException e) {
            throw new RuntimeException("책 조회 실패", e);
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
                        .price(rs.getBigDecimal("price"))
                        .build());
            }
        } catch (SQLException e) {
            throw new RuntimeException("책 목록 조회 실패", e);
        }
        return books;
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB 연결 종료 실패", e);
        }
    }
}
