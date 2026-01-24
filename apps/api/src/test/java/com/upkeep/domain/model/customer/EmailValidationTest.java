package com.upkeep.domain.model.customer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("EmailValidation Utility")
class EmailValidationTest {

    @ParameterizedTest
    @DisplayName("isValid() should return true for valid email formats")
    @ValueSource(strings = {
            "user@example.com",
            "user.name@example.com",
            "user-name@example.com",
            "user_name@example.com",
            "user123@example.co.uk",
            "user@sub.domain.com",
            "a@b.co"
    })
    void isValid_shouldReturnTrueForValidFormats(String validEmail) {
        assertTrue(EmailValidation.isValid(validEmail));
    }

    @ParameterizedTest
    @DisplayName("isValid() should return false for invalid email formats")
    @ValueSource(strings = {
            "invalid",
            "invalid@",
            "@example.com",
            "user@.com",
            "user@example",
            "user@example.c",
            "user example@test.com",
            "user@@example.com",
            ""
    })
    void isValid_shouldReturnFalseForInvalidFormats(String invalidEmail) {
        assertFalse(EmailValidation.isValid(invalidEmail));
    }

    @Test
    @DisplayName("isValid() should return false for null")
    void isValid_shouldReturnFalseForNull() {
        assertFalse(EmailValidation.isValid(null));
    }

    @Test
    @DisplayName("EMAIL_REGEX constant should be defined")
    void emailRegexShouldBeDefined() {
        assertNotNull(EmailValidation.EMAIL_REGEX);
    }

    @Test
    @DisplayName("EMAIL_PATTERN constant should be pre-compiled")
    void emailPatternShouldBePreCompiled() {
        assertNotNull(EmailValidation.EMAIL_PATTERN);
    }
}
