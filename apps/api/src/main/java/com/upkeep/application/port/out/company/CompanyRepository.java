package com.upkeep.application.port.out.company;

import com.upkeep.domain.model.company.Company;
import com.upkeep.domain.model.company.CompanyId;
import com.upkeep.domain.model.company.CompanySlug;

import java.util.Optional;

public interface CompanyRepository {

    Company save(Company company);

    Optional<Company> findById(CompanyId id);

    Optional<Company> findBySlug(CompanySlug slug);

    boolean existsBySlug(CompanySlug slug);
}
