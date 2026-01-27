package com.upkeep.domain.model.company;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("CompanyId")
class CompanyIdTest {

    @Test
    @DisplayName("should generate unique company ID")
    void shouldGenerateUniqueCompanyId() {
        CompanyId id1 = CompanyId.generate();
        CompanyId id2 = CompanyId.generate();

        assertNotNull(id1.value());
        assertNotNull(id2.value());
        assertNotNull(id1.value());
    }

    @Test
    @DisplayName("should create company ID from valid string")
    void shouldCreateFromValidString() {
        UUID uuid = UUID.randomUUID();
        String uuidString = uuid.toString();

        CompanyId companyId = CompanyId.from(uuidString);

        assertEquals(uuid, companyId.value());
    }

    @Test
    @DisplayName("should create company ID from UUID")
    void shouldCreateFromUuid() {
        UUID uuid = UUID.randomUUID();

        CompanyId companyId = CompanyId.from(uuid);

        assertEquals(uuid, companyId.value());
    }

    @Test
    @DisplayName("should throw IllegalArgumentException for invalid UUID string")
    void shouldThrowForInvalidUuidString() {
        assertThrows(IllegalArgumentException.class, () -> CompanyId.from("invalid-uuid"));
    }

    @Test
    @DisplayName("should return string representation of UUID")
    void shouldReturnStringRepresentation() {
        UUID uuid = UUID.randomUUID();
        CompanyId companyId = CompanyId.from(uuid);

        assertEquals(uuid.toString(), companyId.toString());
    }
}
