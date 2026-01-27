package com.upkeep.infrastructure.adapter.out.persistence.membership;

import com.upkeep.application.port.out.membership.MembershipRepository;
import com.upkeep.domain.model.company.CompanyId;
import com.upkeep.domain.model.customer.CustomerId;
import com.upkeep.domain.model.membership.Membership;
import com.upkeep.domain.model.membership.MembershipId;
import com.upkeep.domain.model.membership.Role;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class MembershipJpaRepository implements MembershipRepository, PanacheRepositoryBase<MembershipEntity, UUID> {

    @Override
    public Membership save(Membership membership) {
        MembershipEntity entity = MembershipMapper.toEntity(membership);
        persist(entity);
        return MembershipMapper.toDomain(entity);
    }

    @Override
    public Optional<Membership> findById(MembershipId id) {
        return find("id", id.value())
                .firstResultOptional()
                .map(MembershipMapper::toDomain);
    }

    @Override
    public Optional<Membership> findByCustomerIdAndCompanyId(CustomerId customerId, CompanyId companyId) {
        return find("customerId = ?1 and companyId = ?2", customerId.value(), companyId.value())
                .firstResultOptional()
                .map(MembershipMapper::toDomain);
    }

    @Override
    public List<Membership> findAllByCustomerId(CustomerId customerId) {
        return find("customerId", customerId.value())
                .list()
                .stream()
                .map(MembershipMapper::toDomain)
                .toList();
    }

    @Override
    public List<Membership> findAllByCompanyId(CompanyId companyId) {
        return find("companyId", companyId.value())
                .list()
                .stream()
                .map(MembershipMapper::toDomain)
                .toList();
    }

    @Override
    public long countByCompanyId(CompanyId companyId) {
        return count("companyId", companyId.value());
    }

    @Override
    public long countByCompanyIdAndRole(CompanyId companyId, Role role) {
        return count("companyId = ?1 and role = ?2", companyId.value(), role);
    }

    @Override
    public boolean existsByCustomerIdAndCompanyId(CustomerId customerId, CompanyId companyId) {
        return count("customerId = ?1 and companyId = ?2", customerId.value(), companyId.value()) > 0;
    }

    @Override
    public void delete(Membership membership) {
        delete("id", membership.getId().value());
    }
}
