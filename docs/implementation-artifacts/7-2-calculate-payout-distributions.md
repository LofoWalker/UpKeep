# Story 7.2: Calculate Payout Distributions

Status: ready-for-dev

## Story

As a **system**, I want to calculate how much each maintainer receives, so that payouts can be executed accurately.

## Acceptance Criteria

1. **Given** payout run initiated for period P, **When** calculating, **Then** for each finalized allocation: assign package amount to verified maintainer(s), split equally if multiple, mark HELD_UNCLAIMED if none.

2. **Then** PayoutLineItem created for each: companyId, packageName, maintainerId, amountCents, status.

## Tasks

- [ ] Create `PayoutRun` entity (id, periodMonth, status, createdAt)
- [ ] Create `PayoutLineItem` entity
- [ ] Create `CalculatePayoutDistributionsUseCase`
- [ ] Handle multi-maintainer splits

## Dev Notes

### Domain Model
```java
public class PayoutRun {
    private final PayoutRunId id;
    private final YearMonth periodMonth;
    private PayoutRunStatus status; // CREATED, RUNNING, COMPLETED, FAILED
    private final Instant createdAt;
    private Instant completedAt;
}

public class PayoutLineItem {
    private final PayoutLineItemId id;
    private final PayoutRunId runId;
    private final CompanyId companyId;
    private final String packageName;
    private final UserId maintainerId; // null if unclaimed
    private final long amountCents;
    private PayoutOutcome status;
    private String failureReason;
    private int retryCount;
}

public enum PayoutOutcome {
    PENDING,
    PAID,
    HELD_UNCLAIMED,
    FAILED
}
```

### Database Schema
```sql
CREATE TABLE payout_runs (
    id UUID PRIMARY KEY,
    period_month DATE NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    completed_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE payout_line_items (
    id UUID PRIMARY KEY,
    run_id UUID NOT NULL REFERENCES payout_runs(id),
    company_id UUID NOT NULL REFERENCES companies(id),
    package_name VARCHAR(255) NOT NULL,
    maintainer_id UUID REFERENCES users(id),
    amount_cents BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    failure_reason TEXT,
    retry_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);
```

### E2E Testing Requirements

**Required Playwright tests for this story:**

- [ ] Test: Payout calculation preview shows correct amounts
- [ ] Test: Unclaimed packages show HELD status
- [ ] Test: Claimed packages show maintainer and amount

**Test file location:** `apps/web/e2e/payout-preview.spec.ts`

**Note:** This is primarily backend logic; E2E tests focus on UI display.

### References
- [Source: epics.md#Story-7.2]
- FR27: Calculate distributions

## Dev Agent Record
### Agent Model Used
_To be filled_

