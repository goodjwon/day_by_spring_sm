package com.example.spring.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.Validation;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;


@DisplayName("Book 엔티티 테스트")
public class BookTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }


    @Nested
    @DisplayName("Book 생성 테스트")
    class CreateBookTest {

        @Test
        @DisplayName("유효한 Book 객체 생성 성공")
        void createValidBook() {
            // given
            Book book = Book.builder()
                    .title("Clean Code")
                    .author("Robert C. Martin")
                    .isbn("9780132350884")
                    .price(new BigDecimal("45.99"))
                    .build();

            // when
            Set<ConstraintViolation<Book>> violations = validator.validate(book);

            // then
            assertThat(violations).isEmpty();
            assertThat(book.getTitle()).isEqualTo("Clean Code");
            assertThat(book.getAuthor()).isEqualTo("Robert C. Martin");
            assertThat(book.getIsbn()).isEqualTo("9780132350884");
            assertThat(book.getPrice()).isEqualByComparingTo(new BigDecimal("45.99"));
            assertThat(book.getAvailable()).isTrue(); // Default value
        }

        @Test
        @DisplayName("Builder 패턴으로 Book 생성")
        void createBookWithBuilder() {
            // given & when
            Book book = Book.builder()
                    .title("Effective Java")
                    .author("Joshua Bloch")
                    .isbn("9780134685991")
                    .price(new BigDecimal("52.99"))
                    .available(true)
                    .createdDate(LocalDateTime.now())
                    .build();

            // then
            assertThat(book.getTitle()).isEqualTo("Effective Java");
            assertThat(book.getAuthor()).isEqualTo("Joshua Bloch");
            assertThat(book.getIsbn()).isEqualTo("9780134685991");
            assertThat(book.getPrice()).isEqualByComparingTo(new BigDecimal("52.99"));
            assertThat(book.getAvailable()).isTrue();
            assertThat(book.getCreatedDate()).isNotNull();
        }

        @Test
        @DisplayName("기본값 설정 확인")
        void checkDefaultValues() {
            // given & when
            Book book = Book.builder()
                    .title("Test Book")
                    .author("Test Author")
                    .isbn("9781234567890")
                    .price(new BigDecimal("25.00"))
                    .build();

            // then
            assertThat(book.getAvailable()).isTrue();
            assertThat(book.isDeleted()).isFalse();
            assertThat(book.getDeletedDate()).isNull();
        }
    }

    @Nested
    @DisplayName("Soft Delete 기능 테스트")
    class SoftDeleteTest {

//        @Test
//        @DisplayName("생성된 Book은 삭제되지 않은 상태")

        @Test
        @DisplayName("markAsDeleted() 호출 시 삭제 상태로 변경")
        void restoreShouldClearDeletedState() {
            // given
            Book book = Book.builder()
                    .title("Test Book")
                    .author("Test Author")
                    .isbn("9781234567890")
                    .price(new BigDecimal("25.00"))
                    .build();

            // when
            book.markAsDeleted();

            // then
            assertThat(book.isDeleted()).isTrue();
            assertThat(book.getDeletedDate()).isNotNull();
            assertThat(book.getAvailable()).isFalse();


        }

//        @Test
//        @DisplayName("restore() 호출 시 삭제 상태 해제")
//
//        @Test
//        @DisplayName("삭제 후 복원 시 deletedDate가 정확히 초기화됨")
//
    }

    @Nested
    @DisplayName("Bean Validation 테스트")
    class ValidationTest {
        @Test
        @DisplayName("제목이 null이면 검증 실패")
        void titleShouldNotBeNull() {
            // given
            Book book = Book.builder()
                    .title(null)
                    .author("Test Author")
                    .isbn("9781234567890")
                    .price(new BigDecimal("25.00"))
                    .build();

            // when
            Set<ConstraintViolation<Book>> violations = validator.validate(book);

            // then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage())
                    .isEqualTo("도서 제목은 필수입니다");
        }
    }
}
