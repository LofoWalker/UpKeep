package com.upkeep.application.usecase;

import com.upkeep.application.port.in.pkg.ImportPackagesFromLockfileUseCase;
import com.upkeep.application.port.out.membership.MembershipRepository;
import com.upkeep.application.port.out.pkg.LockfileParser;
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
public class ImportPackagesFromLockfileUseCaseImpl implements ImportPackagesFromLockfileUseCase {

    private final PackageRepository packageRepository;
    private final MembershipRepository membershipRepository;
    private final LockfileParser lockfileParser;

    @Inject
    public ImportPackagesFromLockfileUseCaseImpl(PackageRepository packageRepository,
                                                 MembershipRepository membershipRepository,
                                                 LockfileParser lockfileParser) {
        this.packageRepository = packageRepository;
        this.membershipRepository = membershipRepository;
        this.lockfileParser = lockfileParser;
    }

    @Override
    @Transactional
    public ImportResult execute(ImportFromLockfileCommand command) {
        CompanyId companyId = CompanyId.from(command.companyId());
        CustomerId customerId = CustomerId.from(command.customerId());

        membershipRepository.findByCustomerIdAndCompanyId(customerId, companyId)
                .orElseThrow(() -> new MembershipNotFoundException(command.customerId(), command.companyId()));

        if (!lockfileParser.supports(command.filename())) {
            throw new IllegalArgumentException("Unsupported lockfile format: " + command.filename());
        }

        List<String> parsedNames = lockfileParser.parse(command.fileContent(), command.filename());

        Set<String> existingNames = packageRepository.findExistingNamesByCompanyId(companyId,
                parsedNames.stream().collect(Collectors.toSet()));

        List<String> importedNames = new ArrayList<>();
        List<String> skippedNames = new ArrayList<>();
        List<Package> toSave = new ArrayList<>();

        for (String name : parsedNames) {
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

        return new ImportResult(
                importedNames.size(),
                skippedNames.size(),
                parsedNames.size(),
                importedNames,
                skippedNames
        );
    }
}

