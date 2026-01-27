package com.upkeep.application.port.out.membership;

import com.upkeep.domain.model.company.CompanyId;
import com.upkeep.domain.model.customer.CustomerId;
import com.upkeep.domain.model.membership.Membership;
import com.upkeep.domain.model.membership.MembershipId;
import com.upkeep.domain.model.membership.Role;

import java.util.List;
import java.util.Optional;

public interface MembershipRepository {

    Membership save(Membership membership);

    Optional<Membership> findById(MembershipId id);

    Optional<Membership> findByCustomerIdAndCompanyId(CustomerId customerId, CompanyId companyId);

    List<Membership> findAllByCustomerId(CustomerId customerId);

    List<Membership> findAllByCompanyId(CompanyId companyId);

    long countByCompanyIdAndRole(CompanyId companyId, Role role);

    boolean existsByCustomerIdAndCompanyId(CustomerId customerId, CompanyId companyId);

    void delete(Membership membership);
}
