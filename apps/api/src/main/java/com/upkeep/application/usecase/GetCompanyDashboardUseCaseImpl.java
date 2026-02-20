package com.upkeep.application.usecase;

import com.upkeep.application.port.in.GetCompanyDashboardUseCase;
import com.upkeep.application.port.out.budget.BudgetRepository;
import com.upkeep.application.port.out.company.CompanyRepository;
import com.upkeep.application.port.out.membership.MembershipRepository;
import com.upkeep.application.port.out.pkg.PackageRepository;
import com.upkeep.domain.exception.CompanyNotFoundException;
import com.upkeep.domain.exception.MembershipNotFoundException;
import com.upkeep.domain.model.company.Company;
import com.upkeep.domain.model.company.CompanyId;
import com.upkeep.domain.model.customer.CustomerId;
import com.upkeep.domain.model.membership.Membership;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class GetCompanyDashboardUseCaseImpl implements GetCompanyDashboardUseCase {

    private final CompanyRepository companyRepository;
    private final MembershipRepository membershipRepository;
    private final BudgetRepository budgetRepository;
    private final PackageRepository packageRepository;

    @Inject
    public GetCompanyDashboardUseCaseImpl(CompanyRepository companyRepository,
                                          MembershipRepository membershipRepository,
                                          BudgetRepository budgetRepository,
                                          PackageRepository packageRepository) {
        this.companyRepository = companyRepository;
        this.membershipRepository = membershipRepository;
        this.budgetRepository = budgetRepository;
        this.packageRepository = packageRepository;
    }

    @Override
    public CompanyDashboard execute(GetCompanyDashboardQuery query) {
        CompanyId companyId = CompanyId.from(query.companyId());
        CustomerId customerId = CustomerId.from(query.customerId());

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new CompanyNotFoundException(query.companyId()));

        Membership membership = membershipRepository.findByCustomerIdAndCompanyId(customerId, companyId)
                .orElseThrow(() -> new MembershipNotFoundException(query.customerId(), query.companyId()));

        long totalMembers = membershipRepository.countByCompanyId(companyId);
        boolean hasBudget = budgetRepository.existsByCompanyId(companyId);
        boolean hasPackages = packageRepository.countByCompanyId(companyId) > 0;

        DashboardStats stats = new DashboardStats(
                (int) totalMembers,
                hasBudget,
                hasPackages,
                false
        );

        return new CompanyDashboard(
                company.getId().toString(),
                company.getName().value(),
                company.getSlug().value(),
                membership.getRole(),
                stats
        );
    }
}
