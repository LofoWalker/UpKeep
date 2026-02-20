package com.upkeep.domain.model.pkg;

import com.upkeep.domain.model.company.CompanyId;

import java.time.Instant;
import java.util.regex.Pattern;

public class Package {

    private static final Pattern NPM_PACKAGE_NAME = Pattern.compile(
            "^(@[a-z0-9-~][a-z0-9-._~]*/)?[a-z0-9-~][a-z0-9-._~]*$"
    );

    private final PackageId id;
    private final CompanyId companyId;
    private final String name;
    private final String registry;
    private final Instant importedAt;

    private Package(PackageId id, CompanyId companyId, String name, String registry, Instant importedAt) {
        this.id = id;
        this.companyId = companyId;
        this.name = name;
        this.registry = registry;
        this.importedAt = importedAt;
    }

    public static Package create(CompanyId companyId, String name) {
        validateName(name);
        return new Package(PackageId.generate(), companyId, name, "npm", Instant.now());
    }

    public static Package reconstitute(PackageId id, CompanyId companyId, String name,
                                       String registry, Instant importedAt) {
        return new Package(id, companyId, name, registry, importedAt);
    }

    public static boolean isValidNpmPackageName(String name) {
        if (name == null || name.isBlank()) {
            return false;
        }
        return NPM_PACKAGE_NAME.matcher(name.trim()).matches();
    }

    private static void validateName(String name) {
        if (!isValidNpmPackageName(name)) {
            throw new IllegalArgumentException("Invalid npm package name: " + name);
        }
    }

    public PackageId getId() {
        return id;
    }

    public CompanyId getCompanyId() {
        return companyId;
    }

    public String getName() {
        return name;
    }

    public String getRegistry() {
        return registry;
    }

    public Instant getImportedAt() {
        return importedAt;
    }
}

