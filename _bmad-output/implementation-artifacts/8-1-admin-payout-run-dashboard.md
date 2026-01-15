# Story 8.1: Admin Payout Run Dashboard

Status: ready-for-dev

## Story

As an **internal operator**, I want to view payout run summaries, so that I can monitor the health of the payout system.

## Acceptance Criteria

1. **Given** I am an admin, **When** I access Admin dashboard, **Then** I see payout runs with: period, status, total, paid/held/failed counts, timestamps.

2. **When** I click on a run, **Then** I see breakdown: items by status, failed items with errors, filter/search.

## Tasks

- [ ] Create `GetPayoutRunSummaryUseCase`
- [ ] Create admin authentication/role check
- [ ] Create admin dashboard with runs list
- [ ] Create run detail view with filtering

## Dev Notes

### Admin Route Protection
```java
@Path("/api/admin")
@RolesAllowed("ADMIN")
public class AdminResource { ... }
```

### Payout Run Summary
```java
public record PayoutRunSummary(
    String runId,
    YearMonth period,
    PayoutRunStatus status,
    long totalAmountCents,
    int paidCount,
    int heldCount,
    int failedCount,
    Instant createdAt,
    Instant completedAt
) {}
```

### References
- [Source: architecture.md#UX-Flows-Screen-Architecture] - Admin screens
- [Source: epics.md#Story-8.1]
- FR33: View payout run summaries

## Dev Agent Record
### Agent Model Used
_To be filled_

