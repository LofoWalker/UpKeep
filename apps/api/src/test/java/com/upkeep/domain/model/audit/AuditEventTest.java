package com.upkeep.domain.model.audit;

import com.upkeep.domain.model.budget.Budget;
import com.upkeep.domain.model.budget.Currency;
import com.upkeep.domain.model.budget.Money;
import com.upkeep.domain.model.company.CompanyId;
import com.upkeep.domain.model.customer.CustomerId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("AuditEvent")
class AuditEventTest {

    @Test
    @DisplayName("should create budget created audit event")
    void shouldCreateBudgetCreatedEvent() {
        CompanyId companyId = CompanyId.generate();
        CustomerId actorId = CustomerId.generate();
        Budget budget = Budget.create(companyId, new Money(50000, Currency.EUR));

        Instant before = Instant.now();
        AuditEvent event = AuditEvent.budgetCreated(companyId, actorId, budget);
        Instant after = Instant.now();

        assertNotNull(event.getId());
        assertEquals(companyId, event.getCompanyId());
        assertEquals(AuditEventType.BUDGET_CREATED, event.getEventType());
        assertEquals(actorId, event.getActorId());
        assertEquals("Budget", event.getTargetType());
        assertEquals(budget.getId().toString(), event.getTargetId());
        assertTrue(event.getTimestamp().compareTo(before) >= 0);
        assertTrue(event.getTimestamp().compareTo(after) <= 0);

        Map<String, Object> payload = event.getPayload();
        assertEquals(50000L, payload.get("amountCents"));
        assertEquals("EUR", payload.get("currency"));
        assertNotNull(payload.get("effectiveFrom"));
    }

    @Test
    @DisplayName("should create budget updated audit event")
    void shouldCreateBudgetUpdatedEvent() {
        CompanyId companyId = CompanyId.generate();
        CustomerId actorId = CustomerId.generate();
        Budget budget = Budget.create(companyId, new Money(100000, Currency.EUR));
        long previousAmountCents = 50000L;

        AuditEvent event = AuditEvent.budgetUpdated(companyId, actorId, budget, previousAmountCents);

        assertNotNull(event.getId());
        assertEquals(companyId, event.getCompanyId());
        assertEquals(AuditEventType.BUDGET_UPDATED, event.getEventType());
        assertEquals(actorId, event.getActorId());
        assertEquals("Budget", event.getTargetType());
        assertEquals(budget.getId().toString(), event.getTargetId());

        Map<String, Object> payload = event.getPayload();
        assertEquals(previousAmountCents, payload.get("previousAmountCents"));
        assertEquals(100000L, payload.get("newAmountCents"));
        assertEquals("EUR", payload.get("currency"));
    }

    @Test
    @DisplayName("should reconstitute audit event from persisted data")
    void shouldReconstituteAuditEvent() {
        AuditEventId id = AuditEventId.generate();
        CompanyId companyId = CompanyId.generate();
        AuditEventType eventType = AuditEventType.BUDGET_CREATED;
        CustomerId actorId = CustomerId.generate();
        String targetType = "Budget";
        String targetId = "test-target-id";
        Map<String, Object> payload = Map.of("test", "data");
        Instant timestamp = Instant.now();

        AuditEvent event = AuditEvent.reconstitute(
            id, companyId, eventType, actorId, targetType, targetId, payload, timestamp
        );

        assertEquals(id, event.getId());
        assertEquals(companyId, event.getCompanyId());
        assertEquals(eventType, event.getEventType());
        assertEquals(actorId, event.getActorId());
        assertEquals(targetType, event.getTargetType());
        assertEquals(targetId, event.getTargetId());
        assertEquals(payload, event.getPayload());
        assertEquals(timestamp, event.getTimestamp());
    }

    @Test
    @DisplayName("should return defensive copy of payload")
    void shouldReturnDefensiveCopyOfPayload() {
        CompanyId companyId = CompanyId.generate();
        CustomerId actorId = CustomerId.generate();
        Budget budget = Budget.create(companyId, new Money(50000, Currency.EUR));

        AuditEvent event = AuditEvent.budgetCreated(companyId, actorId, budget);
        Map<String, Object> payload1 = event.getPayload();
        Map<String, Object> payload2 = event.getPayload();

        payload1.put("tampered", "value");

        assertEquals(false, payload2.containsKey("tampered"));
    }
}
