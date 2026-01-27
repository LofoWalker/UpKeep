package com.upkeep.domain.model.company;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Company")
class CompanyTest {

    @Test
    @DisplayName("should create company with generated ID and timestamps")
    void shouldCreateCompany() {
        CompanyName name = new CompanyName("Acme Inc");
        CompanySlug slug = new CompanySlug("acme-inc");

        Instant before = Instant.now();
        Company company = Company.create(name, slug);
        Instant after = Instant.now();

        assertNotNull(company.getId());
        assertEquals(name, company.getName());
        assertEquals(slug, company.getSlug());
        assertTrue(company.getCreatedAt().compareTo(before) >= 0);
        assertTrue(company.getCreatedAt().compareTo(after) <= 0);
        assertEquals(company.getCreatedAt(), company.getUpdatedAt());
    }

    @Test
    @DisplayName("should reconstitute company from persisted data")
    void shouldReconstituteCompany() {
        CompanyId id = CompanyId.generate();
        CompanyName name = new CompanyName("Test Company");
        CompanySlug slug = new CompanySlug("test-company");
        Instant createdAt = Instant.now().minusSeconds(3600);
        Instant updatedAt = Instant.now();

        Company company = Company.reconstitute(id, name, slug, createdAt, updatedAt);

        assertEquals(id, company.getId());
        assertEquals(name, company.getName());
        assertEquals(slug, company.getSlug());
        assertEquals(createdAt, company.getCreatedAt());
        assertEquals(updatedAt, company.getUpdatedAt());
    }

    @Test
    @DisplayName("should update timestamp")
    void shouldUpdateTimestamp() {
        Company company = Company.create(new CompanyName("Test"), new CompanySlug("test"));
        Instant originalUpdatedAt = company.getUpdatedAt();

        company.updateTimestamp();

        assertTrue(company.getUpdatedAt().compareTo(originalUpdatedAt) >= 0);
    }
}
