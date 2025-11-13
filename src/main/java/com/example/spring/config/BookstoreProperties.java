package com.example.spring.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 타입 안전한 설정 관리
 */
@Configuration
@ConfigurationProperties(prefix = "bookstore")
@Data
public class BookstoreProperties {
    private Email email = new Email();
    private Order order = new Order();

    @Data
    public static class Email {
        private Smtp smtp = new Smtp();

        @Data
        public static class Smtp {
            private String host = "localhost";
            private int port = 587;
            private boolean enabled = true;
            private String from = "bookstore@example.com";
            private String username;
            private String password;
        }
    }

    @Data
    public static class Order {
        private int maxBooksPerOrder = 10;
        private double defaultDiscountRate = 0.0;
    }
}
