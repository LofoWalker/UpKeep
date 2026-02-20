package com.upkeep.infrastructure.adapter.out.persistence.pkg;

import com.upkeep.domain.model.company.CompanyId;
import com.upkeep.domain.model.pkg.Package;
import com.upkeep.domain.model.pkg.PackageId;

public final class PackageMapper {

    private PackageMapper() {
    }

    public static PackageEntity toEntity(Package pkg) {
        PackageEntity entity = new PackageEntity();
        entity.id = pkg.getId().value();
        entity.companyId = pkg.getCompanyId().value();
        entity.name = pkg.getName();
        entity.registry = pkg.getRegistry();
        entity.importedAt = pkg.getImportedAt();
        return entity;
    }

    public static Package toDomain(PackageEntity entity) {
        return Package.reconstitute(
                PackageId.from(entity.id),
                CompanyId.from(entity.companyId),
                entity.name,
                entity.registry,
                entity.importedAt
        );
    }
}

