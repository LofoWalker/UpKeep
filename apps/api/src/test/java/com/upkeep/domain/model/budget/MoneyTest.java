package com.upkeep.domain.model.budget;
import com.upkeep.domain.exception.DomainValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;
@DisplayName("Money")
class MoneyTest {
    @Test
    @DisplayName("should create money with cents and currency")
    void shouldCreateMoney() {
        Money money = new Money(50000, Currency.EUR);
        assertEquals(50000, money.amountCents());
        assertEquals(Currency.EUR, money.currency());
    }
    @Test
    @DisplayName("should create money from decimal amount")
    void shouldCreateMoneyFromDecimal() {
        Money money = Money.of(new BigDecimal("500.00"), Currency.USD);
        assertEquals(50000, money.amountCents());
        assertEquals(Currency.USD, money.currency());
    }
    @Test
    @DisplayName("should reject negative amount in constructor")
    void shouldRejectNegativeAmount() {
        assertThrows(DomainValidationException.class, () -> 
            new Money(-100, Currency.EUR)
        );
    }
}
