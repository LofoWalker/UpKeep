# Story 4.4: View Allocation History

Status: ready-for-dev

## Story

As a **company Member**, I want to view past allocations, so that I can see how our funding evolved over time.

## Acceptance Criteria

1. **Given** allocations have been finalized, **When** I go to Allocations page, **Then** I see list with month, total, package count, status.

2. **When** I click on a past allocation, **Then** I see breakdown: each package with amount, percentage, who finalized, when.

3. **Given** I use MonthNavigator, **When** I select a month, **Then** view shows that month's allocation.

## Tasks

- [ ] Create `ListAllocationHistoryUseCase`
- [ ] Create `GetAllocationSnapshotUseCase`
- [ ] Create MonthNavigator component
- [ ] Create allocation detail view

## Dev Notes

### MonthNavigator Component
```tsx
interface MonthNavigatorProps {
  currentMonth: string // "2026-01"
  onChange: (month: string) => void
  availableMonths: string[]
}

export function MonthNavigator({ currentMonth, onChange, availableMonths }: MonthNavigatorProps) {
  return (
    <div className="flex items-center gap-2">
      <Button variant="ghost" size="icon" onClick={() => navigate(-1)}>
        <ChevronLeftIcon />
      </Button>
      <span className="font-medium">{formatMonth(currentMonth)}</span>
      <Button variant="ghost" size="icon" onClick={() => navigate(+1)}>
        <ChevronRightIcon />
      </Button>
    </div>
  )
}
```


### References
- [Source: epics.md#Story-4.4]

## Dev Agent Record
### Agent Model Used
_To be filled_

