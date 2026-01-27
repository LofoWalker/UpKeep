package com.upkeep.domain.model.company;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("CompanyName")
class CompanyNameTest {

    @Test
    @DisplayName("should create company name with valid value")
    void shouldCreateWithValidValue() {
        CompanyName name = new CompanyName("Acme Inc");

        assertEquals("Acme Inc", name.value());
    }

    @Test
    @DisplayName("should trim company name")
    void shouldTrimCompanyName() {
        CompanyName name = CompanyName.from("  Acme Inc  ");

        assertEquals("Acme Inc", name.value());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    @DisplayName("should throw IllegalArgumentException for empty or blank names")
    void shouldThrowForEmptyOrBlankNames(String value) {
        assertThrows(IllegalArgumentException.class, () -> new CompanyName(value));
    }

    @Test
    @DisplayName("should throw IllegalArgumentException for name too short")
    void shouldThrowForNameTooShort() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new CompanyName("A")
        );
        assertEquals("Company name must be between 2 and 100 characters", exception.getMessage());
    }

    @Test
    @DisplayName("should throw IllegalArgumentException for name too long")
    void shouldThrowForNameTooLong() {
        String longName = "A".repeat(101);
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new CompanyName(longName)
        );
        assertEquals("Company name must be between 2 and 100 characters", exception.getMessage());
    }

    @Test
    @DisplayName("should accept name with exactly 2 characters")
    void shouldAcceptMinLengthName() {
        CompanyName name = new CompanyName("AB");

        assertEquals("AB", name.value());
    }

    @Test
    @DisplayName("should accept name with exactly 100 characters")
    void shouldAcceptMaxLengthName() {
        String maxName = "A".repeat(100);
        CompanyName name = new CompanyName(maxName);

        assertEquals(maxName, name.value());
    }

    @Test
    @DisplayName("should return string representation")
    void shouldReturnStringRepresentation() {
        CompanyName name = new CompanyName("Test Company");

        assertEquals("Test Company", name.toString());
    }
}
