# Story 8.5: Held/Unclaimed Funds Explanation

Status: ready-for-dev

## Story

As a **support user**, I want clear explanations for held funds, so that I can help users understand outcomes.

## Acceptance Criteria

1. **Given** package allocation is HELD_UNCLAIMED, **When** I view explanation, **Then** I see: "No verified maintainer", claim history, suggested next steps.

2. **Given** I am a company member viewing held funds, **Then** I see user-friendly explanation and option to reallocate next month.

## Tasks

- [ ] Create `GetHeldFundsExplanationUseCase`
- [ ] Reusable explanations for admin and user views
- [ ] Add explanation tooltip/modal to UI

## Dev Notes

### Explanation Generator
```java
public HeldFundsExplanation explain(String packageName, PayoutLineItem item) {
    List<PackageClaim> claims = claimRepository.findByPackageName(packageName);
    
    return new HeldFundsExplanation(
        "No verified maintainer for this package",
        claims.isEmpty() 
            ? "No one has claimed this package yet." 
            : "There are " + claims.size() + " pending claims awaiting verification.",
        List.of(
            "Wait for a maintainer to claim and verify the package",
            "Reallocate funds to other packages next month"
        )
    );
}
```


### References
- [Source: epics.md#Story-8.5]
- FR36: Explain held/unclaimed funds

## Dev Agent Record
### Agent Model Used
_To be filled_

