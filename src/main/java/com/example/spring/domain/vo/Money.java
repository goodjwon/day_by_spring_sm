package com.example.spring.domain.vo;

import com.example.spring.exception.ErrorMessages;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * 금액을 나타내는 Value Object
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
public class Money {

    public static final Money ZERO = Money.of(BigDecimal.ZERO);
    public static final String DEFAULT_CURRENCY = "KRW";

    @Column(name = "amount", precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", length = 3)
    private String currency;

    private Money(BigDecimal amount, String currency) {
        validateAmount(amount);
        validateCurrency(currency);
        this.amount = amount.setScale(2, RoundingMode.HALF_UP);
        this.currency = currency;
    }

    public static Money of(BigDecimal amount) {
        return new Money(amount, DEFAULT_CURRENCY);
    }

    public static Money of(BigDecimal amount, String currency) {
        return new Money(amount, currency);
    }

    public static Money of(long amount) {
        return new Money(BigDecimal.valueOf(amount), DEFAULT_CURRENCY);
    }

    public static Money of(long amount, String currency) {
        return new Money(BigDecimal.valueOf(amount), currency);
    }

    public static Money zero() {
        return ZERO;
    }

    public Money add(Money other) {
        validateSameCurrency(other);
        return new Money(this.amount.add(other.amount), this.currency);
    }

    public Money subtract(Money other) {
        validateSameCurrency(other);
        return new Money(this.amount.subtract(other.amount), this.currency);
    }

    public Money multiply(int quantity) {
        return new Money(this.amount.multiply(BigDecimal.valueOf(quantity)), this.currency);
    }

    public Money multiply(BigDecimal multiplier) {
        return new Money(this.amount.multiply(multiplier), this.currency);
    }

    public boolean isGreaterThan(Money other) {
        validateSameCurrency(other);
        return this.amount.compareTo(other.amount) > 0;
    }

    public boolean isGreaterThanOrEqual(Money other) {
        validateSameCurrency(other);
        return this.amount.compareTo(other.amount) >= 0;
    }

    public boolean isLessThan(Money other) {
        validateSameCurrency(other);
        return this.amount.compareTo(other.amount) < 0;
    }

    public boolean isLessThanOrEqual(Money other) {
        validateSameCurrency(other);
        return this.amount.compareTo(other.amount) <= 0;
    }

    public boolean isZero() {
        return this.amount.compareTo(BigDecimal.ZERO) == 0;
    }

    public boolean isPositive() {
        return this.amount.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean isNegative() {
        return this.amount.compareTo(BigDecimal.ZERO) < 0;
    }

    public Money divide(int divisor) {
        if (divisor == 0) {
            throw new IllegalArgumentException(ErrorMessages.MONEY_DIVIDE_BY_ZERO);
        }
        return new Money(this.amount.divide(BigDecimal.valueOf(divisor), 2, RoundingMode.HALF_UP), this.currency);
    }

    public int compareTo(Money other) {
        validateSameCurrency(other);
        return this.amount.compareTo(other.amount);
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null) {
            throw new IllegalArgumentException(ErrorMessages.MONEY_AMOUNT_NULL);
        }
    }

    private void validateCurrency(String currency) {
        if (currency == null || currency.isBlank()) {
            throw new IllegalArgumentException(ErrorMessages.MONEY_CURRENCY_REQUIRED);
        }
        if (currency.length() != 3) {
            throw new IllegalArgumentException(ErrorMessages.MONEY_CURRENCY_LENGTH);
        }
    }

    private void validateSameCurrency(Money other) {
        if (other == null) {
            throw new IllegalArgumentException(ErrorMessages.MONEY_OTHER_NULL);
        }
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException(ErrorMessages.moneyCurrencyMismatch(this.currency, other.currency));
        }
    }

    @Override
    public String toString() {
        return String.format("%s %s", amount.toPlainString(), currency);
    }
}