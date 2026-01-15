# Story 4.3: Finalize Allocation

Status: ready-for-dev

## Story

As a **company Owner**, I want to finalize a monthly allocation, so that it becomes the official allocation for payouts.

## Acceptance Criteria

1. **Given** I am an Owner viewing a valid draft, **When** I click "Finalize", **Then** status changes to FINALIZED, AllocationSnapshot is created, audit event recorded (FR38), allocation becomes read-only.

2. **Given** the draft violates guardrails, **When** I try to finalize, **Then** I see errors and finalization is blocked.

3. **Given** I am a Member, **When** I try to finalize, **Then** I see "Only Owners can finalize allocations".

## Tasks

- [ ] Create `FinalizeAllocationUseCase` with guardrail validation
- [ ] Create `AllocationSnapshot` entity for immutable record
- [ ] Create audit event for finalization (FR38)
- [ ] Add Owner-only finalize button in UI

## Dev Notes

### AllocationSnapshot Entity
```java
public class AllocationSnapshot {
    private final AllocationSnapshotId id;
    private final CompanyId companyId;
    private final YearMonth periodMonth;
    private final AllocationStatus status; // Always FINALIZED
    private final List<AllocationLineItem> lineItems;
    private final Instant finalizedAt;
    private final UserId finalizedBy;
}
```

### Use Case
```java
@Transactional
public AllocationSnapshot execute(FinalizeCommand command) {
    // Verify Owner
    verifyOwnerRole(command.actorUserId(), command.companyId());
    
    AllocationDraft draft = draftRepository.findById(command.draftId())
        .orElseThrow();
    
    // Validate guardrails
    ValidationResult validation = guardrailValidator.validate(draft);
    if (!validation.isValid()) {
        throw new DomainRuleException("Guardrails violated: " + validation.violations());
    }
    
    // Create snapshot
    AllocationSnapshot snapshot = AllocationSnapshot.fromDraft(draft, command.actorUserId());
    snapshotRepository.save(snapshot);
    
    // Update draft status
    draft.finalize();
    draftRepository.save(draft);
    
    // Audit event
    auditEventRepository.save(AuditEvent.allocationFinalized(snapshot));
    
    return snapshot;
}
```

### References
- [Source: epics.md#Story-4.3]
- FR38: Audit event for finalization
- State: DRAFT → FINALIZED (irreversible)

## Dev Agent Record
### Agent Model Used
_To be filled_

