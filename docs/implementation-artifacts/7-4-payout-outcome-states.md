# Story 7.4: Payout Outcome States

Status: ready-for-dev

## Story

As a **platform user**, I want explicit payout states, so that I always know what happened to funds.

## Acceptance Criteria

1. **Given** a payout line item, **When** I view status, **Then** it shows: PAID, HELD_UNCLAIMED, FAILED (with reason), or PENDING.

2. **Then** PayoutStatusBadge displays status with appropriate color/icon.

## Tasks

- [ ] Create `PayoutOutcome` enum with explanations
- [ ] Create PayoutStatusBadge component
- [ ] Add user-friendly status messages

## Dev Notes

### PayoutStatusBadge
```tsx
const statusConfig = {
  PAID: { color: 'success', icon: CheckCircle, label: 'Paid' },
  HELD_UNCLAIMED: { color: 'warning', icon: Clock, label: 'Held - Unclaimed' },
  FAILED: { color: 'error', icon: XCircle, label: 'Failed' },
  PENDING: { color: 'muted', icon: Clock, label: 'Pending' },
}

export function PayoutStatusBadge({ status, reason }: Props) {
  const config = statusConfig[status]
  return (
    <Badge variant={config.color}>
      <config.icon className="h-3 w-3 mr-1" />
      {config.label}
      {reason && <span className="ml-1 text-xs">({reason})</span>}
    </Badge>
  )
}
```

### E2E Testing Requirements

**Required Playwright tests for this story:**

- [ ] Test: Payout status badges display correct colors and labels
- [ ] Test: COMPLETED, FAILED, HELD states render correctly
- [ ] Test: Failure reason shown for failed payouts

**Test file location:** `apps/web/e2e/payout-status.spec.ts`

### References
- [Source: epics.md#Story-7.4]
- FR29: Payout outcome states
- NFR10: Never unknown money state

## Dev Agent Record
### Agent Model Used
_To be filled_

