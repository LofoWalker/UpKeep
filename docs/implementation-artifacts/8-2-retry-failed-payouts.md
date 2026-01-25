# Story 8.2: Retry Failed Payouts

Status: ready-for-dev

## Story

As an **internal operator**, I want to retry failed payouts, so that temporary failures can be resolved.

## Acceptance Criteria

1. **Given** run with FAILED items, **When** I select and click "Retry", **Then** items are re-processed and status updates.

2. **Given** item fails again, **Then** failure count increments, after 3 failures flagged for manual review.

## Tasks

- [ ] Create `RetryFailedPayoutsUseCase`
- [ ] Track retryCount on PayoutLineItem
- [ ] Add retry button in admin UI
- [ ] Flag items needing manual review

## Dev Notes

### Retry Logic
```java
@Transactional
public void execute(List<String> lineItemIds) {
    for (String id : lineItemIds) {
        PayoutLineItem item = repository.findById(id);
        if (item.getStatus() != PayoutOutcome.FAILED) continue;
        
        try {
            stripeAdapter.transfer(item);
            item.setStatus(PayoutOutcome.PAID);
        } catch (Exception e) {
            item.incrementRetryCount();
            if (item.getRetryCount() >= 3) {
                item.flagForManualReview();
            }
        }
        repository.save(item);
    }
}
```

### E2E Testing Requirements

**Required Playwright tests for this story:**

- [ ] Test: Admin can select failed payout items for retry
- [ ] Test: Retry button triggers re-processing
- [ ] Test: Manual review flag displayed after max retries

**Test file location:** `apps/web/e2e/admin-payout-dashboard.spec.ts` (extend existing file)

### References
- [Source: epics.md#Story-8.2]
- FR34: Retry failed payouts

## Dev Agent Record
### Agent Model Used
_To be filled_

