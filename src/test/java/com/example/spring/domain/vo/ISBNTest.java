package com.example.spring.domain.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ISBNTest {

    @Nested
    @DisplayName("ISBN 생성")
    class Creation {

        @Test
        @DisplayName("하이픈이 있는 ISBN-13 형식")
        void createWithHyphenatedISBN13() {
            ISBN isbn = ISBN.of("978-89-7050-123-4");

            assertThat(isbn.getValue()).isEqualTo("978-89-7050-123-4");
        }

        @Test
        @DisplayName("하이픈이 없는 13자리 ISBN")
        void createWithPlainISBN13() {
            ISBN isbn = ISBN.of("9788970501234");

            assertThat(isbn.getValue()).contains("978");
            assertThat(isbn.getDigitsOnly()).isEqualTo("9788970501234");
        }

        @Test
        @DisplayName("ISBN 접두사가 있는 형식")
        void createWithISBNPrefix() {
            ISBN isbn = ISBN.of("ISBN9788970501234");

            assertThat(isbn.getValue()).isEqualTo("ISBN9788970501234");
        }

        @ParameterizedTest
        @DisplayName("유효한 ISBN 형식들")
        @ValueSource(strings = {
                "978-89-7050-123-4",
                "9788970501234",
                "ISBN9788970501234"
        })
        void validISBNFormats(String isbnString) {
            ISBN isbn = ISBN.of(isbnString);

            assertThat(isbn).isNotNull();
        }
    }

    @Nested
    @DisplayName("유효성 검증")
    class Validation {

        @Test
        @DisplayName("null ISBN으로 생성 시 예외 발생")
        void createWithNull() {
            assertThatThrownBy(() -> ISBN.of(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("ISBN은 필수입니다");
        }

        @Test
        @DisplayName("빈 ISBN으로 생성 시 예외 발생")
        void createWithEmpty() {
            assertThatThrownBy(() -> ISBN.of(""))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("ISBN은 필수입니다");
        }

        @Test
        @DisplayName("유효하지 않은 형식으로 생성 시 예외 발생")
        void createWithInvalidFormat() {
            assertThatThrownBy(() -> ISBN.of("invalid-isbn"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("올바른 ISBN 형식이 아닙니다");
        }
    }

    @Nested
    @DisplayName("getDigitsOnly")
    class DigitsOnly {

        @Test
        @DisplayName("하이픈이 있는 ISBN에서 숫자만 추출")
        void getDigitsFromHyphenatedISBN() {
            ISBN isbn = ISBN.of("978-89-7050-123-4");

            assertThat(isbn.getDigitsOnly()).isEqualTo("9788970501234");
        }

        @Test
        @DisplayName("ISBN 접두사가 있는 경우 숫자만 추출")
        void getDigitsFromPrefixedISBN() {
            ISBN isbn = ISBN.of("ISBN9788970501234");

            assertThat(isbn.getDigitsOnly()).isEqualTo("9788970501234");
        }
    }

    @Nested
    @DisplayName("equals 및 hashCode")
    class Equality {

        @Test
        @DisplayName("같은 값을 가진 ISBN은 동등함")
        void equalsWithSameValue() {
            ISBN isbn1 = ISBN.of("9788970501234");
            ISBN isbn2 = ISBN.of("9788970501234");

            assertThat(isbn1).isEqualTo(isbn2);
            assertThat(isbn1.hashCode()).isEqualTo(isbn2.hashCode());
        }

        @Test
        @DisplayName("다른 값을 가진 ISBN은 동등하지 않음")
        void notEqualsWithDifferentValue() {
            ISBN isbn1 = ISBN.of("9788970501234");
            ISBN isbn2 = ISBN.of("9788970501235");

            assertThat(isbn1).isNotEqualTo(isbn2);
        }
    }

    @Test
    @DisplayName("toString은 ISBN 값을 반환")
    void toStringReturnsValue() {
        ISBN isbn = ISBN.of("ISBN9788970501234");

        assertThat(isbn.toString()).isEqualTo("ISBN9788970501234");
    }
}