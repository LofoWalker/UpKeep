package com.upkeep.application.usecase;

import com.upkeep.application.port.in.CreateCompanyUseCase;
import com.upkeep.application.port.out.company.CompanyRepository;
import com.upkeep.application.port.out.membership.MembershipRepository;
import com.upkeep.domain.exception.CompanySlugAlreadyExistsException;
import com.upkeep.domain.model.company.Company;
import com.upkeep.domain.model.company.CompanyName;
import com.upkeep.domain.model.company.CompanySlug;
import com.upkeep.domain.model.customer.CustomerId;
import com.upkeep.domain.model.membership.Membership;
import com.upkeep.domain.model.membership.Role;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class CreateCompanyUseCaseImpl implements CreateCompanyUseCase {

    private final CompanyRepository companyRepository;
    private final MembershipRepository membershipRepository;

    @Inject
    public CreateCompanyUseCaseImpl(CompanyRepository companyRepository,
                                    MembershipRepository membershipRepository) {
        this.companyRepository = companyRepository;
        this.membershipRepository = membershipRepository;
    }

    @Override
    @Transactional
    public CreateCompanyResult execute(CreateCompanyCommand command) {
        CompanyName name = CompanyName.from(command.name());
        CompanySlug slug = command.slug() != null && !command.slug().isBlank()
                ? CompanySlug.from(command.slug())
                : CompanySlug.fromName(command.name());

        if (companyRepository.existsBySlug(slug)) {
            throw new CompanySlugAlreadyExistsException(slug.value());
        }

        Company company = Company.create(name, slug);
        Company savedCompany = companyRepository.save(company);

        CustomerId customerId = CustomerId.from(command.customerId());
        Membership membership = Membership.create(customerId, savedCompany.getId(), Role.OWNER);
        Membership savedMembership = membershipRepository.save(membership);

        return new CreateCompanyResult(
                savedCompany.getId().toString(),
                savedCompany.getName().value(),
                savedCompany.getSlug().value(),
                new MembershipInfo(
                        savedMembership.getId().toString(),
                        savedMembership.getRole()
                )
        );
    }
}
