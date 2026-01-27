package com.upkeep.application.usecase;

import com.upkeep.application.port.in.GetCompanyMembersUseCase;
import com.upkeep.application.port.out.customer.CustomerRepository;
import com.upkeep.application.port.out.membership.MembershipRepository;
import com.upkeep.domain.exception.MembershipNotFoundException;
import com.upkeep.domain.model.company.CompanyId;
import com.upkeep.domain.model.customer.Customer;
import com.upkeep.domain.model.customer.CustomerId;
import com.upkeep.domain.model.membership.Membership;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class GetCompanyMembersUseCaseImpl implements GetCompanyMembersUseCase {

    private final MembershipRepository membershipRepository;
    private final CustomerRepository customerRepository;

    @Inject
    public GetCompanyMembersUseCaseImpl(MembershipRepository membershipRepository,
                                        CustomerRepository customerRepository) {
        this.membershipRepository = membershipRepository;
        this.customerRepository = customerRepository;
    }

    @Override
    public List<MemberInfo> execute(GetCompanyMembersQuery query) {
        CustomerId customerId = CustomerId.from(query.customerId());
        CompanyId companyId = CompanyId.from(query.companyId());

        membershipRepository.findByCustomerIdAndCompanyId(customerId, companyId)
                .orElseThrow(() -> new MembershipNotFoundException(query.customerId(), query.companyId()));

        List<Membership> memberships = membershipRepository.findAllByCompanyId(companyId);

        return memberships.stream()
                .map(membership -> {
                    Optional<Customer> customer = customerRepository.findById(membership.getCustomerId());
                    String email = customer.map(c -> c.getEmail().value()).orElse("unknown");
                    return new MemberInfo(
                            membership.getId().toString(),
                            membership.getCustomerId().toString(),
                            email,
                            membership.getRole(),
                            membership.getJoinedAt()
                    );
                })
                .toList();
    }
}
