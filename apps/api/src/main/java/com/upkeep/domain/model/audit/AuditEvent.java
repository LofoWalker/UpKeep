package com.upkeep.domain.model.audit;

import com.upkeep.domain.model.budget.Budget;
import com.upkeep.domain.model.company.CompanyId;
import com.upkeep.domain.model.customer.CustomerId;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Audit event tracking all important actions in the system for compliance and transparency (FR37).
 */
public class AuditEvent {
    private final AuditEventId id;
    private final CompanyId companyId;
    private final AuditEventType eventType;
    private final CustomerId actorId;
    private final String targetType;
    private final String targetId;
    private final Map<String, Object> payload;
    private final Instant timestamp;

    private AuditEvent(AuditEventId id,
                       CompanyId companyId,
                       AuditEventType eventType,
                       CustomerId actorId,
                       String targetType,
                       String targetId,
                       Map<String, Object> payload,
                       Instant timestamp) {
        this.id = id;
        this.companyId = companyId;
        this.eventType = eventType;
        this.actorId = actorId;
        this.targetType = targetType;
        this.targetId = targetId;
        this.payload = new HashMap<>(payload);
        this.timestamp = timestamp;
    }

    public static AuditEvent budgetCreated(CompanyId companyId, CustomerId actorId, Budget budget) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("amountCents", budget.getAmount().amountCents());
        payload.put("currency", budget.getAmount().currency().name());
        payload.put("effectiveFrom", budget.getEffectiveFrom().toString());

        return new AuditEvent(
                AuditEventId.generate(),
                companyId,
                AuditEventType.BUDGET_CREATED,
                actorId,
                "Budget",
                budget.getId().toString(),
                payload,
                Instant.now()
        );
    }

    public static AuditEvent budgetUpdated(CompanyId companyId, CustomerId actorId, Budget budget, long previousAmountCents) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("previousAmountCents", previousAmountCents);
        payload.put("newAmountCents", budget.getAmount().amountCents());
        payload.put("currency", budget.getAmount().currency().name());

        return new AuditEvent(
                AuditEventId.generate(),
                companyId,
                AuditEventType.BUDGET_UPDATED,
                actorId,
                "Budget",
                budget.getId().toString(),
                payload,
                Instant.now()
        );
    }

    public static AuditEvent reconstitute(AuditEventId id,
                                          CompanyId companyId,
                                          AuditEventType eventType,
                                          CustomerId actorId,
                                          String targetType,
                                          String targetId,
                                          Map<String, Object> payload,
                                          Instant timestamp) {
        return new AuditEvent(id, companyId, eventType, actorId, targetType, targetId, payload, timestamp);
    }

    public AuditEventId getId() {
        return id;
    }

    public CompanyId getCompanyId() {
        return companyId;
    }

    public AuditEventType getEventType() {
        return eventType;
    }

    public CustomerId getActorId() {
        return actorId;
    }

    public String getTargetType() {
        return targetType;
    }

    public String getTargetId() {
        return targetId;
    }

    public Map<String, Object> getPayload() {
        return new HashMap<>(payload);
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}
