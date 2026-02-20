package com.upkeep.application.port.out.pkg;

import com.upkeep.domain.model.company.CompanyId;
import com.upkeep.domain.model.pkg.Package;

import java.util.List;
import java.util.Set;

public interface PackageRepository {

    void save(Package pkg);

    void saveAll(List<Package> packages);

    List<Package> findByCompanyId(CompanyId companyId, int offset, int limit);

    List<Package> findByCompanyIdAndNameContaining(CompanyId companyId, String search, int offset, int limit);

    long countByCompanyId(CompanyId companyId);

    long countByCompanyIdAndNameContaining(CompanyId companyId, String search);

    Set<String> findExistingNamesByCompanyId(CompanyId companyId, Set<String> names);

    boolean existsByCompanyIdAndName(CompanyId companyId, String name);
}

