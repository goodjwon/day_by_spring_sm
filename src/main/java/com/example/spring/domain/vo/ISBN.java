package com.example.spring.domain.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.regex.Pattern;

/**
 * ISBN을 나타내는 Value Object
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
public class ISBN {

    private static final Pattern ISBN_PATTERN = Pattern.compile(
            "^\\d{3}-?\\d{1,5}-?\\d{1,7}-?\\d{1,7}-?\\d{1}$|^\\d{13}$|^ISBN\\d+$"
    );

    @Column(name = "isbn", nullable = false, unique = true, length = 17)
    private String value;

    private ISBN(String value) {
        validate(value);
        this.value = normalize(value);
    }

    public static ISBN of(String value) {
        return new ISBN(value);
    }

    private void validate(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("ISBN은 필수입니다");
        }
        if (!ISBN_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("올바른 ISBN 형식이 아닙니다: " + value);
        }
    }

    private String normalize(String value) {
        // ISBN 접두사가 있으면 그대로 유지
        if (value.startsWith("ISBN")) {
            return value;
        }
        // 이미 하이픈이 포함된 경우 그대로 유지
        if (value.contains("-")) {
            return value;
        }
        // 하이픈이 없는 13자리 숫자만 있는 경우 표준 형식으로 변환
        if (value.length() == 13) {
            return formatISBN13(value);
        }
        return value;
    }

    private String formatISBN13(String digits) {
        // 표준 ISBN-13 형식: 978-X-XX-XXXXXX-X
        return String.format("%s-%s-%s-%s-%s",
                digits.substring(0, 3),
                digits.substring(3, 4),
                digits.substring(4, 6),
                digits.substring(6, 12),
                digits.substring(12, 13));
    }

    public String getDigitsOnly() {
        return value.replaceAll("[^0-9]", "");
    }

    @Override
    public String toString() {
        return value;
    }
}
