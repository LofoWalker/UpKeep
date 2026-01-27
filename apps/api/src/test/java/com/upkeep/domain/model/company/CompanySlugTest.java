package com.upkeep.domain.model.company;

import com.upkeep.domain.exception.DomainValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("CompanySlug")
class CompanySlugTest {

    @Test
    @DisplayName("should create slug with valid value")
    void shouldCreateWithValidValue() {
        CompanySlug slug = new CompanySlug("acme-inc");

        assertEquals("acme-inc", slug.value());
    }

    @Test
    @DisplayName("should create slug from value with conversion to lowercase")
    void shouldConvertToLowercase() {
        CompanySlug slug = CompanySlug.from("ACME-INC");

        assertEquals("acme-inc", slug.value());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    @DisplayName("should throw DomainValidationException for empty or blank slugs")
    void shouldThrowForEmptyOrBlankSlugs(String value) {
        DomainValidationException exception = assertThrows(
                DomainValidationException.class,
                () -> new CompanySlug(value)
        );
        assertFalse(exception.getFieldErrors().isEmpty());
        assertEquals("slug", exception.getFieldErrors().get(0).field());
    }

    @Test
    @DisplayName("should throw DomainValidationException for slug too short")
    void shouldThrowForSlugTooShort() {
        DomainValidationException exception = assertThrows(
                DomainValidationException.class,
                () -> new CompanySlug("a")
        );
        assertEquals("Company slug must be between 2 and 50 characters", exception.getMessage());
        assertEquals("slug", exception.getFieldErrors().get(0).field());
    }

    @Test
    @DisplayName("should throw DomainValidationException for slug too long")
    void shouldThrowForSlugTooLong() {
        String longSlug = "a".repeat(51);
        DomainValidationException exception = assertThrows(
                DomainValidationException.class,
                () -> new CompanySlug(longSlug)
        );
        assertEquals("Company slug must be between 2 and 50 characters", exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"Acme Inc", "acme_inc", "acme.inc", "acme@inc", "-acme", "acme-", "--acme"})
    @DisplayName("should throw DomainValidationException for invalid slug format")
    void shouldThrowForInvalidFormat(String value) {
        DomainValidationException exception = assertThrows(
                DomainValidationException.class,
                () -> new CompanySlug(value)
        );
        assertEquals("slug", exception.getFieldErrors().get(0).field());
    }

    @Test
    @DisplayName("should accept slug with exactly 2 characters")
    void shouldAcceptMinLengthSlug() {
        CompanySlug slug = new CompanySlug("ab");

        assertEquals("ab", slug.value());
    }

    @Test
    @DisplayName("should accept slug with exactly 50 characters")
    void shouldAcceptMaxLengthSlug() {
        String maxSlug = "a".repeat(50);
        CompanySlug slug = new CompanySlug(maxSlug);

        assertEquals(maxSlug, slug.value());
    }

    @Test
    @DisplayName("should generate slug from company name")
    void shouldGenerateSlugFromName() {
        CompanySlug slug = CompanySlug.fromName("Acme Inc");

        assertEquals("acme-inc", slug.value());
    }

    @Test
    @DisplayName("should generate slug from name with special characters")
    void shouldGenerateSlugFromNameWithSpecialChars() {
        CompanySlug slug = CompanySlug.fromName("Acme & Co. Ltd!");

        assertEquals("acme-co-ltd", slug.value());
    }

    @Test
    @DisplayName("should generate slug from name with multiple spaces")
    void shouldGenerateSlugFromNameWithMultipleSpaces() {
        CompanySlug slug = CompanySlug.fromName("My   Awesome   Company");

        assertEquals("my-awesome-company", slug.value());
    }

    @Test
    @DisplayName("should add suffix for very short generated slugs")
    void shouldAddSuffixForShortSlugs() {
        CompanySlug slug = CompanySlug.fromName("A");

        assertEquals("a-co", slug.value());
    }

    @Test
    @DisplayName("should truncate very long generated slugs")
    void shouldTruncateLongSlugs() {
        String longName = "A".repeat(100);
        CompanySlug slug = CompanySlug.fromName(longName);

        assertEquals(50, slug.value().length());
    }

    @Test
    @DisplayName("should return string representation")
    void shouldReturnStringRepresentation() {
        CompanySlug slug = new CompanySlug("test-company");

        assertEquals("test-company", slug.toString());
    }
}
