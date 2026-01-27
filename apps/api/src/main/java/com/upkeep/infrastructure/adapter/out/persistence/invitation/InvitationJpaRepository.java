package com.upkeep.infrastructure.adapter.out.persistence.invitation;

import com.upkeep.application.port.out.invitation.InvitationRepository;
import com.upkeep.domain.model.company.CompanyId;
import com.upkeep.domain.model.customer.Email;
import com.upkeep.domain.model.invitation.Invitation;
import com.upkeep.domain.model.invitation.InvitationId;
import com.upkeep.domain.model.invitation.InvitationStatus;
import com.upkeep.domain.model.invitation.InvitationToken;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class InvitationJpaRepository implements InvitationRepository, PanacheRepositoryBase<InvitationEntity, UUID> {

    @Override
    public Invitation save(Invitation invitation) {
        InvitationEntity entity = InvitationMapper.toEntity(invitation);
        InvitationEntity managed = getEntityManager().merge(entity);
        return InvitationMapper.toDomain(managed);
    }

    @Override
    public Optional<Invitation> findById(InvitationId id) {
        return find("id", id.value())
                .firstResultOptional()
                .map(InvitationMapper::toDomain);
    }

    @Override
    public Optional<Invitation> findByToken(InvitationToken token) {
        return find("token", token.value())
                .firstResultOptional()
                .map(InvitationMapper::toDomain);
    }

    @Override
    public Optional<Invitation> findByCompanyIdAndEmailAndStatus(CompanyId companyId,
                                                                 Email email,
                                                                 InvitationStatus status) {
        return find("companyId = ?1 and email = ?2 and status = ?3", companyId.value(), email.value(), status)
                .firstResultOptional()
                .map(InvitationMapper::toDomain);
    }

    @Override
    public List<Invitation> findAllByCompanyId(CompanyId companyId) {
        return find("companyId", companyId.value())
                .list()
                .stream()
                .map(InvitationMapper::toDomain)
                .toList();
    }

    @Override
    public List<Invitation> findAllByCompanyIdAndStatus(CompanyId companyId, InvitationStatus status) {
        return find("companyId = ?1 and status = ?2", companyId.value(), status)
                .list()
                .stream()
                .map(InvitationMapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsByCompanyIdAndEmailAndStatus(CompanyId companyId, Email email, InvitationStatus status) {
        return count("companyId = ?1 and email = ?2 and status = ?3",
                companyId.value(), email.value(), status) > 0;
    }
}
