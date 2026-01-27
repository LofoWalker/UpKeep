package com.upkeep.infrastructure.adapter.out.persistence.company;

import com.upkeep.domain.model.company.Company;
import com.upkeep.domain.model.company.CompanyId;
import com.upkeep.domain.model.company.CompanyName;
import com.upkeep.domain.model.company.CompanySlug;

public final class CompanyMapper {

    private CompanyMapper() {
    }

    public static CompanyEntity toEntity(Company company) {
        CompanyEntity entity = new CompanyEntity();
        entity.id = company.getId().value();
        entity.name = company.getName().value();
        entity.slug = company.getSlug().value();
        entity.createdAt = company.getCreatedAt();
        entity.updatedAt = company.getUpdatedAt();
        return entity;
    }

    public static Company toDomain(CompanyEntity entity) {
        return Company.reconstitute(
                CompanyId.from(entity.id),
                CompanyName.from(entity.name),
                CompanySlug.from(entity.slug),
                entity.createdAt,
                entity.updatedAt
        );
    }
}
