package com.upkeep.application.usecase;

import com.upkeep.application.port.in.GetUserCompaniesUseCase;
import com.upkeep.application.port.out.company.CompanyRepository;
import com.upkeep.application.port.out.membership.MembershipRepository;
import com.upkeep.domain.model.company.Company;
import com.upkeep.domain.model.customer.CustomerId;
import com.upkeep.domain.model.membership.Membership;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class GetUserCompaniesUseCaseImpl implements GetUserCompaniesUseCase {

    private final MembershipRepository membershipRepository;
    private final CompanyRepository companyRepository;

    @Inject
    public GetUserCompaniesUseCaseImpl(MembershipRepository membershipRepository,
                                        CompanyRepository companyRepository) {
        this.membershipRepository = membershipRepository;
        this.companyRepository = companyRepository;
    }

    @Override
    public List<CompanyWithMembership> execute(GetUserCompaniesQuery query) {
        CustomerId customerId = CustomerId.from(query.customerId());
        List<Membership> memberships = membershipRepository.findAllByCustomerId(customerId);

        return memberships.stream()
                .map(membership -> {
                    Optional<Company> company = companyRepository.findById(membership.getCompanyId());
                    return company.map(c -> new CompanyWithMembership(
                            c.getId().toString(),
                            c.getName().value(),
                            c.getSlug().value(),
                            membership.getRole()
                    )).orElse(null);
                })
                .filter(java.util.Objects::nonNull)
                .toList();
    }
}
