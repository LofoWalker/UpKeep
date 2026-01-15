# Story 4.1: Create Allocation Draft

Status: ready-for-dev

## Story

As a **company Member**, I want to create a monthly allocation draft, so that I can propose how to distribute our budget across packages.

## Acceptance Criteria

1. **Given** I am on Allocations page, **When** I click "New Allocation" for current month, **Then** a draft is created with AllocationEditor showing packages, BudgetBar, and GuardrailBadge.

2. **Given** a draft already exists for this month, **When** I go to Allocations, **Then** I can continue editing.

## Tasks

- [ ] Create `AllocationDraft` entity (id, companyId, periodMonth, status, lineItems)
- [ ] Create `AllocationLineItem` (packageId, amountCents, percentage)
- [ ] Create `CreateAllocationDraftUseCase`
- [ ] Create AllocationEditor UI component

## Dev Notes

### Domain Model
```java
public class AllocationDraft {
    private final AllocationDraftId id;
    private final CompanyId companyId;
    private final YearMonth periodMonth;
    private AllocationStatus status; // DRAFT, FINALIZED
    private List<AllocationLineItem> lineItems;
    private final Instant createdAt;
    private Instant updatedAt;
}

public class AllocationLineItem {
    private final PackageId packageId;
    private long amountCents;
    private BigDecimal percentage;
}
```

### Database Schema
```sql
CREATE TABLE allocation_drafts (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL REFERENCES companies(id),
    period_month DATE NOT NULL, -- First day of month
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    UNIQUE(company_id, period_month)
);

CREATE TABLE allocation_line_items (
    id UUID PRIMARY KEY,
    allocation_draft_id UUID NOT NULL REFERENCES allocation_drafts(id),
    package_id UUID NOT NULL REFERENCES packages(id),
    amount_cents BIGINT NOT NULL,
    percentage DECIMAL(5,2) NOT NULL,
    UNIQUE(allocation_draft_id, package_id)
);
```

### References
- [Source: epics.md#Story-4.1]
- State machine: DRAFT → FINALIZED

## Dev Agent Record
### Agent Model Used
_To be filled_

