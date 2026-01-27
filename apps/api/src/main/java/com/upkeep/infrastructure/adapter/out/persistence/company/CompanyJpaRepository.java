package com.upkeep.infrastructure.adapter.out.persistence.company;

import com.upkeep.application.port.out.company.CompanyRepository;
import com.upkeep.domain.model.company.Company;
import com.upkeep.domain.model.company.CompanyId;
import com.upkeep.domain.model.company.CompanySlug;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class CompanyJpaRepository implements CompanyRepository, PanacheRepositoryBase<CompanyEntity, UUID> {

    @Override
    public Company save(Company company) {
        CompanyEntity entity = CompanyMapper.toEntity(company);
        persist(entity);
        return CompanyMapper.toDomain(entity);
    }

    @Override
    public Optional<Company> findById(CompanyId id) {
        return find("id", id.value())
                .firstResultOptional()
                .map(CompanyMapper::toDomain);
    }

    @Override
    public Optional<Company> findBySlug(CompanySlug slug) {
        return find("slug", slug.value())
                .firstResultOptional()
                .map(CompanyMapper::toDomain);
    }

    @Override
    public boolean existsBySlug(CompanySlug slug) {
        return count("slug", slug.value()) > 0;
    }
}
