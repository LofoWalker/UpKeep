package com.upkeep.domain.model.pkg;

import com.upkeep.domain.model.company.CompanyId;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PackageTest {

    @Test
    void shouldCreatePackageWithValidName() {
        CompanyId companyId = CompanyId.generate();
        Package pkg = Package.create(companyId, "lodash");

        assertNotNull(pkg.getId());
        assertEquals(companyId, pkg.getCompanyId());
        assertEquals("lodash", pkg.getName());
        assertEquals("npm", pkg.getRegistry());
        assertNotNull(pkg.getImportedAt());
    }

    @Test
    void shouldCreateScopedPackage() {
        CompanyId companyId = CompanyId.generate();
        Package pkg = Package.create(companyId, "@types/node");

        assertEquals("@types/node", pkg.getName());
    }

    @Test
    void shouldRejectInvalidPackageName() {
        CompanyId companyId = CompanyId.generate();
        assertThrows(IllegalArgumentException.class, () -> Package.create(companyId, "INVALID NAME!!"));
    }

    @Test
    void shouldRejectNullName() {
        assertFalse(Package.isValidNpmPackageName(null));
    }

    @Test
    void shouldRejectBlankName() {
        assertFalse(Package.isValidNpmPackageName(""));
        assertFalse(Package.isValidNpmPackageName("   "));
    }

    @Test
    void shouldValidateNpmPackageNames() {
        assertTrue(Package.isValidNpmPackageName("lodash"));
        assertTrue(Package.isValidNpmPackageName("express"));
        assertTrue(Package.isValidNpmPackageName("@types/node"));
        assertTrue(Package.isValidNpmPackageName("@babel/core"));
        assertTrue(Package.isValidNpmPackageName("my-package"));
        assertTrue(Package.isValidNpmPackageName("my_package"));
        assertTrue(Package.isValidNpmPackageName("my.package"));

        assertFalse(Package.isValidNpmPackageName("UPPERCASE"));
        assertFalse(Package.isValidNpmPackageName("has spaces"));
        assertFalse(Package.isValidNpmPackageName(".starts-with-dot"));
    }
}

