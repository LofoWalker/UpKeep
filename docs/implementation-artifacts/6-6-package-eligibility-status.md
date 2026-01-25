# Story 6.6: Package Eligibility Status

Status: ready-for-dev

## Story

As a **platform operator**, I want the system to track package eligibility, so that payouts only go to verified maintainers.

## Acceptance Criteria

1. **Given** package has at least one VERIFIED claim, **Then** package is ELIGIBLE for payouts.

2. **Given** package has no verified claims, **Then** package is UNCLAIMED and allocations result in HELD_UNCLAIMED status.

## Tasks

- [ ] Create `CheckPackageEligibilityUseCase`
- [ ] Add eligibility status derivation from claims
- [ ] Used by payout system to determine outcomes

## Dev Notes

### Eligibility Logic
```java
public EligibilityStatus getEligibility(String packageName) {
    boolean hasVerifiedClaim = claimRepository
        .existsByPackageNameAndStatus(packageName, ClaimStatus.VERIFIED);
    
    return hasVerifiedClaim 
        ? EligibilityStatus.ELIGIBLE 
        : EligibilityStatus.UNCLAIMED;
}

public enum EligibilityStatus {
    ELIGIBLE,
    UNCLAIMED
}
```

### E2E Testing Requirements

**Required Playwright tests for this story:**

- [ ] Test: Eligible packages show ELIGIBLE status
- [ ] Test: Unclaimed packages show UNCLAIMED status
- [ ] Test: Status updates after successful claim verification

**Test file location:** `apps/web/e2e/maintainer-dashboard.spec.ts` (extend existing file)

### References
- [Source: epics.md#Story-6.6]
- FR26: Package eligibility status

## Dev Agent Record
### Agent Model Used
_To be filled_

