# Story 6.3: Initiate Package Claim

Status: ready-for-dev

## Story

As a **maintainer**, I want to claim an npm package, so that I can receive funding allocated to it.

## Acceptance Criteria

1. **Given** I click "Claim a Package", **When** I enter package name, **Then** system validates on npm, creates claim with status PENDING, shows verification options.

2. **Given** package doesn't exist, **Then** I see "Package not found on npm".

3. **Given** package already claimed by verified maintainer, **Then** I see "This package is already claimed."

## Tasks

- [ ] Create `PackageClaim` entity (id, maintainerId, packageName, status, createdAt)
- [ ] Create `InitiatePackageClaimUseCase`
- [ ] Create `NpmRegistryAdapter` to validate package existence
- [ ] Create claim UI with search

## Dev Notes

### Domain Model
```java
public class PackageClaim {
    private final PackageClaimId id;
    private final UserId maintainerId;
    private final String packageName;
    private ClaimStatus status; // PENDING, VERIFIED, REJECTED
    private final Instant createdAt;
}

public enum ClaimStatus {
    PENDING,
    VERIFIED,
    REJECTED
}
```

### NpmRegistryAdapter
```java
public interface NpmRegistryAdapter {
    boolean packageExists(String packageName);
    PackageInfo getPackageInfo(String packageName);
}

// HTTP call to https://registry.npmjs.org/{package}
```

### Database Schema
```sql
CREATE TABLE package_claims (
    id UUID PRIMARY KEY,
    maintainer_id UUID NOT NULL REFERENCES users(id),
    package_name VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_package_claims__maintainer ON package_claims(maintainer_id);
CREATE UNIQUE INDEX idx_package_claims__verified ON package_claims(package_name) WHERE status = 'VERIFIED';
```

### E2E Testing Requirements

**Required Playwright tests for this story:**

- [ ] Create Page Object Model for package claim (`e2e/pages/package-claim.ts`)
- [ ] Test: Maintainer can initiate claim for a package
- [ ] Test: Search for unclaimed packages works
- [ ] Test: Claim status shows as PENDING after initiation

**Test file location:** `apps/web/e2e/package-claim.spec.ts`

### References
- [Source: epics.md#Story-6.3]
- FR23: Initiate package claim

## Dev Agent Record
### Agent Model Used
_To be filled_

