package com.upkeep.infrastructure.adapter.out.persistence.audit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.upkeep.domain.model.audit.AuditEvent;
import com.upkeep.domain.model.audit.AuditEventId;
import com.upkeep.domain.model.audit.AuditEventType;
import com.upkeep.domain.model.company.CompanyId;
import com.upkeep.domain.model.customer.CustomerId;

import java.util.HashMap;
import java.util.Map;

public final class AuditEventMapper {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final TypeReference<Map<String, Object>> MAP_TYPE_REF = new TypeReference<>() {};

    private AuditEventMapper() {
    }

    public static AuditEventEntity toEntity(AuditEvent auditEvent) {
        AuditEventEntity entity = new AuditEventEntity();
        entity.id = auditEvent.getId().value();
        entity.companyId = auditEvent.getCompanyId() != null ? auditEvent.getCompanyId().value() : null;
        entity.eventType = auditEvent.getEventType().name();
        entity.actorId = auditEvent.getActorId() != null ? auditEvent.getActorId().value() : null;
        entity.targetType = auditEvent.getTargetType();
        entity.targetId = auditEvent.getTargetId();
        entity.payload = serializePayload(auditEvent.getPayload());
        entity.timestamp = auditEvent.getTimestamp();
        return entity;
    }

    public static AuditEvent toDomain(AuditEventEntity entity) {
        return AuditEvent.reconstitute(
                AuditEventId.from(entity.id),
                entity.companyId != null ? CompanyId.from(entity.companyId) : null,
                AuditEventType.valueOf(entity.eventType),
                entity.actorId != null ? CustomerId.from(entity.actorId) : null,
                entity.targetType,
                entity.targetId,
                deserializePayload(entity.payload),
                entity.timestamp
        );
    }

    private static String serializePayload(Map<String, Object> payload) {
        try {
            return OBJECT_MAPPER.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize audit event payload", e);
        }
    }

    private static Map<String, Object> deserializePayload(String payload) {
        try {
            if (payload == null || payload.isEmpty()) {
                return new HashMap<>();
            }
            return OBJECT_MAPPER.readValue(payload, MAP_TYPE_REF);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize audit event payload", e);
        }
    }
}
