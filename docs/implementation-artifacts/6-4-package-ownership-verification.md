# Story 6.4: Package Ownership Verification

Status: ready-for-dev

## Story

As a **maintainer**, I want to verify my package ownership, so that my claim can be approved.

## Acceptance Criteria

1. **Given** pending claim, **When** I view verification options, **Then** I see: GitHub collaborator access, npm publish access, manual review request.

2. **Given** I choose GitHub verification and have push access, **Then** claim status → VERIFIED, audit event recorded (FR40).

3. **Given** verification fails, **Then** I see reason and can try another method.

## Tasks

- [ ] Create `ClaimVerification` entity (claimId, method, status, verifiedAt)
- [ ] Create `VerifyPackageClaimUseCase`
- [ ] Create `GitHubAdapter` to check collaborator access
- [ ] Create verification UI flow

## Dev Notes

### Verification Methods
```java
public enum VerificationMethod {
    GITHUB_COLLABORATOR,
    NPM_PUBLISH_ACCESS,
    MANUAL_REVIEW
}

public class ClaimVerification {
    private final ClaimVerificationId id;
    private final PackageClaimId claimId;
    private final VerificationMethod method;
    private VerificationStatus status;
    private String failureReason;
    private final Instant attemptedAt;
    private Instant verifiedAt;
}
```

### GitHub Verification Flow
1. User connects GitHub OAuth (if not already)
2. Get package's repository from npm registry
3. Check if user has push access to repo via GitHub API
4. If yes → VERIFIED

### E2E Testing Requirements

**Required Playwright tests for this story:**

- [ ] Test: Verification instructions displayed for pending claim
- [ ] Test: GitHub OAuth flow initiates correctly
- [ ] Test: Verification success updates claim status to VERIFIED
- [ ] Test: Verification failure shows error message

**Test file location:** `apps/web/e2e/package-claim.spec.ts` (extend existing file)

**Note:** Mock GitHub API responses for E2E tests.

### References
- [Source: epics.md#Story-6.4]
- FR24, FR40: Claim verification with audit

## Dev Agent Record
### Agent Model Used
_To be filled_

