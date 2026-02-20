package com.upkeep.infrastructure.adapter.out.persistence.pkg;

import com.upkeep.application.port.out.pkg.PackageRepository;
import com.upkeep.domain.model.company.CompanyId;
import com.upkeep.domain.model.pkg.Package;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@ApplicationScoped
public class PackageJpaRepository implements PackageRepository, PanacheRepositoryBase<PackageEntity, UUID> {

    @Override
    public void save(Package pkg) {
        persist(PackageMapper.toEntity(pkg));
    }

    @Override
    public void saveAll(List<Package> packages) {
        List<PackageEntity> entities = packages.stream()
                .map(PackageMapper::toEntity)
                .toList();
        persist(entities);
    }

    @Override
    public List<Package> findByCompanyId(CompanyId companyId, int offset, int limit) {
        return find("companyId = ?1 ORDER BY name ASC", companyId.value())
                .page(offset / Math.max(limit, 1), limit)
                .list()
                .stream()
                .map(PackageMapper::toDomain)
                .toList();
    }

    @Override
    public List<Package> findByCompanyIdAndNameContaining(CompanyId companyId, String search, int offset, int limit) {
        return find("companyId = ?1 AND LOWER(name) LIKE LOWER(?2) ORDER BY name ASC",
                companyId.value(), "%" + search + "%")
                .page(offset / Math.max(limit, 1), limit)
                .list()
                .stream()
                .map(PackageMapper::toDomain)
                .toList();
    }

    @Override
    public long countByCompanyId(CompanyId companyId) {
        return count("companyId", companyId.value());
    }

    @Override
    public long countByCompanyIdAndNameContaining(CompanyId companyId, String search) {
        return count("companyId = ?1 AND LOWER(name) LIKE LOWER(?2)",
                companyId.value(), "%" + search + "%");
    }

    @Override
    public Set<String> findExistingNamesByCompanyId(CompanyId companyId, Set<String> names) {
        if (names.isEmpty()) {
            return Set.of();
        }
        List<String> existing = getEntityManager()
                .createQuery("SELECT p.name FROM PackageEntity p WHERE p.companyId = :companyId AND p.name IN :names",
                        String.class)
                .setParameter("companyId", companyId.value())
                .setParameter("names", names)
                .getResultList();
        return new HashSet<>(existing);
    }

    @Override
    public boolean existsByCompanyIdAndName(CompanyId companyId, String name) {
        return count("companyId = ?1 AND name = ?2", companyId.value(), name) > 0;
    }
}

