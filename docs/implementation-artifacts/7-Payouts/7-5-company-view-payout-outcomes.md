# Story 7.5: Company View Payout Outcomes

Status: ready-for-dev

## Story

As a **company Member**, I want to see payout outcomes for my allocations, so that I know where my funding went.

## Acceptance Criteria

1. **Given** I view an allocation, **Then** each package shows: amount allocated, payout status, explanation if HELD_UNCLAIMED ("Awaiting maintainer claim") or FAILED.

2. **Given** Allocations overview, **Then** I see aggregates: total paid, total held, total failed.

## Tasks

- [ ] Create `GetAllocationPayoutStatusUseCase`
- [ ] Join AllocationSnapshot with PayoutLineItems
- [ ] Add payout status column to allocation view
- [ ] Show aggregate stats

## Dev Notes

### Allocation with Payout Status
```java
public record AllocationWithPayoutStatus(
    String packageName,
    long allocatedCents,
    PayoutOutcome payoutStatus,
    String explanation
) {}

// Explanation examples:
// HELD_UNCLAIMED: "Awaiting maintainer claim"
// FAILED: "Transfer failed: insufficient funds"
// PAID: "Paid to @maintainer"
```


### References
- [Source: epics.md#Story-7.5]
- FR30: View payout outcomes

## Dev Agent Record
### Agent Model Used
_To be filled_

