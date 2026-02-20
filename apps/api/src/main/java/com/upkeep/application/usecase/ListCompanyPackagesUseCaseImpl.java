package com.upkeep.application.usecase;

import com.upkeep.application.port.in.pkg.ListCompanyPackagesUseCase;
import com.upkeep.application.port.out.membership.MembershipRepository;
import com.upkeep.application.port.out.pkg.PackageRepository;
import com.upkeep.domain.exception.MembershipNotFoundException;
import com.upkeep.domain.model.company.CompanyId;
import com.upkeep.domain.model.customer.CustomerId;
import com.upkeep.domain.model.pkg.Package;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

@ApplicationScoped
public class ListCompanyPackagesUseCaseImpl implements ListCompanyPackagesUseCase {

    private final PackageRepository packageRepository;
    private final MembershipRepository membershipRepository;

    @Inject
    public ListCompanyPackagesUseCaseImpl(PackageRepository packageRepository,
                                          MembershipRepository membershipRepository) {
        this.packageRepository = packageRepository;
        this.membershipRepository = membershipRepository;
    }

    @Override
    public PackageListResult execute(ListPackagesQuery query) {
        CompanyId companyId = CompanyId.from(query.companyId());
        CustomerId customerId = CustomerId.from(query.customerId());

        membershipRepository.findByCustomerIdAndCompanyId(customerId, companyId)
                .orElseThrow(() -> new MembershipNotFoundException(query.customerId(), query.companyId()));

        int offset = query.page() * query.size();

        List<Package> packages;
        long totalCount;

        if (query.search() != null && !query.search().isBlank()) {
            packages = packageRepository.findByCompanyIdAndNameContaining(companyId, query.search(), offset, query.size());
            totalCount = packageRepository.countByCompanyIdAndNameContaining(companyId, query.search());
        } else {
            packages = packageRepository.findByCompanyId(companyId, offset, query.size());
            totalCount = packageRepository.countByCompanyId(companyId);
        }

        List<PackageItem> items = packages.stream()
                .map(pkg -> new PackageItem(
                        pkg.getId().toString(),
                        pkg.getName(),
                        pkg.getRegistry(),
                        pkg.getImportedAt().toString()
                ))
                .toList();

        return new PackageListResult(items, totalCount, query.page(), query.size());
    }
}

