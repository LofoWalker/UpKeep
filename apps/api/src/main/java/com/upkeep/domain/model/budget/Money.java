package com.upkeep.domain.model.budget;

import com.upkeep.domain.exception.DomainValidationException;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Value object representing a monetary amount with currency.
 * Amount is stored in cents to avoid floating-point precision issues.
 */
public record Money(long amountCents, Currency currency) {

    public Money {
        if (amountCents < 0) {
            throw new DomainValidationException("Amount cannot be negative");
        }
        Objects.requireNonNull(currency, "Currency cannot be null");
    }

    public static Money of(BigDecimal amount, Currency currency) {
        if (amount == null) {
            throw new DomainValidationException("Amount cannot be null");
        }
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new DomainValidationException("Amount cannot be negative");
        }
        long cents = amount.multiply(BigDecimal.valueOf(100)).longValue();
        return new Money(cents, currency);
    }

    public static Money zero(Currency currency) {
        return new Money(0, currency);
    }

    public BigDecimal toDecimal() {
        return BigDecimal.valueOf(amountCents).divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
    }

    public Money add(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new DomainValidationException("Cannot add money with different currencies");
        }
        return new Money(this.amountCents + other.amountCents, this.currency);
    }

    public Money subtract(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new DomainValidationException("Cannot subtract money with different currencies");
        }
        long result = this.amountCents - other.amountCents;
        if (result < 0) {
            throw new DomainValidationException("Result cannot be negative");
        }
        return new Money(result, this.currency);
    }

    public boolean isGreaterThan(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new DomainValidationException("Cannot compare money with different currencies");
        }
        return this.amountCents > other.amountCents;
    }

    public boolean isLessThan(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new DomainValidationException("Cannot compare money with different currencies");
        }
        return this.amountCents < other.amountCents;
    }
}
