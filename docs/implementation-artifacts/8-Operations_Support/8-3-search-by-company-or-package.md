# Story 8.3: Search by Company or Package

Status: ready-for-dev

## Story

As an **internal support user**, I want to search by company or package, so that I can investigate issues quickly.

## Acceptance Criteria

1. **Given** I search by company name/ID, **Then** I see: company details, all allocations, all payout outcomes.

2. **Given** I search by package name, **Then** I see: package details, claim status/history, all allocations, payout history.

## Tasks

- [ ] Create `SearchCompanyUseCase`
- [ ] Create `SearchPackageUseCase`
- [ ] Implement full-text search with filters
- [ ] Create search UI with results display

## Dev Notes

### Search Results
```java
public record CompanySearchResult(
    CompanyDetails company,
    List<AllocationSummary> allocations,
    List<PayoutOutcome> payouts
) {}

public record PackageSearchResult(
    PackageDetails package,
    List<ClaimInfo> claims,
    List<AllocationInfo> allocations,
    List<PayoutInfo> payouts
) {}
```


### References
- [Source: epics.md#Story-8.3]
- FR35: Search by company/package

## Dev Agent Record
### Agent Model Used
_To be filled_

