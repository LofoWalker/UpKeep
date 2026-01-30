package com.upkeep.infrastructure.adapter.out.persistence.audit;

import com.upkeep.application.port.out.audit.AuditEventRepository;
import com.upkeep.domain.model.audit.AuditEvent;
import com.upkeep.domain.model.audit.AuditEventId;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class AuditEventJpaRepository implements AuditEventRepository, PanacheRepositoryBase<AuditEventEntity, UUID> {

    @Override
    public void save(AuditEvent auditEvent) {
        AuditEventEntity entity = AuditEventMapper.toEntity(auditEvent);
        persist(entity);
    }

    @Override
    public Optional<AuditEvent> findById(AuditEventId id) {
        return find("id", id.value())
                .firstResultOptional()
                .map(AuditEventMapper::toDomain);
    }
}
