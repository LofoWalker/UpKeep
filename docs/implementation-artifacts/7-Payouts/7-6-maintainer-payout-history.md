# Story 7.6: Maintainer Payout History

Status: ready-for-dev

## Story

As a **maintainer**, I want to see my payout history, so that I can track my earnings.

## Acceptance Criteria

1. **Given** I go to Payouts section, **Then** I see: total earnings, this month's expected, history with date/amount/package/status.

2. **When** I click on a payout, **Then** I see details with aggregate company contributions (not individual amounts for privacy).

## Tasks

- [ ] Create `GetMaintainerPayoutHistoryUseCase`
- [ ] Aggregate without exposing individual company amounts
- [ ] Create payout history list
- [ ] Create payout detail view

## Dev Notes

### Privacy
```java
// DO NOT expose individual company allocation amounts
// Show aggregated contribution count instead
public record PayoutDetail(
    String packageName,
    long amountCents,
    int contributingCompaniesCount, // "Funded by 5 companies"
    PayoutOutcome status,
    Instant paidAt
) {}
```

### E2E Testing Requirements

**Required Playwright tests for this story:**

- [ ] Test: Maintainer sees payout history with amounts
- [ ] Test: Contributing companies count shown (not individual amounts)
- [ ] Test: Historical data loads correctly with pagination

**Test file location:** `apps/web/e2e/maintainer-payouts.spec.ts`

### References
- [Source: epics.md#Story-7.6]
- FR32: Maintainer payout history

## Dev Agent Record
### Agent Model Used
_To be filled_

