package com.upkeep.application.usecase;

import com.upkeep.application.port.in.pkg.ImportPackagesFromListUseCase;
import com.upkeep.application.port.out.membership.MembershipRepository;
import com.upkeep.application.port.out.pkg.PackageRepository;
import com.upkeep.domain.exception.MembershipNotFoundException;
import com.upkeep.domain.model.company.CompanyId;
import com.upkeep.domain.model.customer.CustomerId;
import com.upkeep.domain.model.pkg.Package;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
public class ImportPackagesFromListUseCaseImpl implements ImportPackagesFromListUseCase {

    private final PackageRepository packageRepository;
    private final MembershipRepository membershipRepository;

    @Inject
    public ImportPackagesFromListUseCaseImpl(PackageRepository packageRepository,
                                             MembershipRepository membershipRepository) {
        this.packageRepository = packageRepository;
        this.membershipRepository = membershipRepository;
    }

    @Override
    @Transactional
    public ImportListResult execute(ImportFromListCommand command) {
        CompanyId companyId = CompanyId.from(command.companyId());
        CustomerId customerId = CustomerId.from(command.customerId());

        membershipRepository.findByCustomerIdAndCompanyId(customerId, companyId)
                .orElseThrow(() -> new MembershipNotFoundException(command.customerId(), command.companyId()));

        List<String> validNames = new ArrayList<>();
        List<String> invalidNames = new ArrayList<>();

        for (String rawName : command.packageNames()) {
            String trimmed = rawName.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            if (Package.isValidNpmPackageName(trimmed)) {
                validNames.add(trimmed);
            } else {
                invalidNames.add(trimmed);
            }
        }

        Set<String> existingNames = packageRepository.findExistingNamesByCompanyId(companyId,
                validNames.stream().collect(Collectors.toSet()));

        List<String> importedNames = new ArrayList<>();
        List<String> skippedNames = new ArrayList<>();
        List<Package> toSave = new ArrayList<>();

        for (String name : validNames) {
            if (existingNames.contains(name)) {
                skippedNames.add(name);
            } else {
                Package pkg = Package.create(companyId, name);
                toSave.add(pkg);
                importedNames.add(name);
            }
        }

        if (!toSave.isEmpty()) {
            packageRepository.saveAll(toSave);
        }

        return new ImportListResult(
                importedNames.size(),
                skippedNames.size(),
                invalidNames.size(),
                importedNames,
                skippedNames,
                invalidNames
        );
    }
}

