# Story 3.2: Update Monthly Budget

Status: ready-for-dev

## Story

As a **company Owner**, I want to update my company's monthly budget, so that I can adjust our open-source spending.

## Acceptance Criteria

1. **Given** a budget exists, **When** I click "Edit Budget" and enter a new amount, **Then** the budget is updated and an audit event is recorded.

2. **Given** I try to set a budget lower than current allocations, **When** I confirm, **Then** I see a warning and can choose to proceed or cancel.

## Tasks

- [ ] Create `UpdateCompanyBudgetUseCase` with validation against current allocations
- [ ] Add audit event for budget updates
- [ ] Create edit budget UI with warning dialog
- [ ] Update REST endpoint to handle PATCH

## Dev Notes

- UseCase: `UpdateCompanyBudgetUseCase`
- Budget changes take effect for next allocation period
- Audit event type: `BUDGET_UPDATED`
- Show warning if newBudget < currentAllocations

### E2E Testing Requirements

**Required Playwright tests for this story:**

- [ ] Test: User can update existing budget
- [ ] Test: Warning dialog shown when reducing below current allocations
- [ ] Test: User can confirm or cancel budget reduction

**Test file location:** `apps/web/e2e/budget.spec.ts` (extend existing file)

### References
- [Source: epics.md#Story-3.2]

## Dev Agent Record
### Agent Model Used
_To be filled_

