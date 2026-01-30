package com.upkeep.application.port.out.audit;

import com.upkeep.domain.model.audit.AuditEvent;
import com.upkeep.domain.model.audit.AuditEventId;

import java.util.Optional;

public interface AuditEventRepository {
    void save(AuditEvent auditEvent);

    Optional<AuditEvent> findById(AuditEventId id);
}
