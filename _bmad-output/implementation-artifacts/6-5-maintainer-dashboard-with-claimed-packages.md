# Story 6.5: Maintainer Dashboard with Claimed Packages

Status: ready-for-dev

## Story

As a **maintainer**, I want to see all my claimed packages, so that I can track my claims and expected payouts.

## Acceptance Criteria

1. **Given** I view my dashboard, **Then** I see claims with: package name, status (PENDING/VERIFIED/REJECTED), expected payout, total received.

2. **Given** no claims, **Then** I see EmptyState with CTA to claim packages.

## Tasks

- [ ] Create `GetMaintainerDashboardUseCase`
- [ ] Aggregate payout expectations from all allocations
- [ ] Create maintainer dashboard page
- [ ] Create claimed packages list

## Dev Notes

### Dashboard Data
```java
public record MaintainerDashboard(
    List<ClaimInfo> claims,
    Money totalEarnings,
    Money expectedThisMonth
) {}

public record ClaimInfo(
    String claimId,
    String packageName,
    ClaimStatus status,
    Money expectedPayout,
    Money totalReceived
) {}
```

### References
- [Source: epics.md#Story-6.5]
- FR25: View claimed packages

## Dev Agent Record
### Agent Model Used
_To be filled_

