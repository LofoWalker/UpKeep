# Story 4.2: Edit Allocation with Real-time Guardrails

Status: ready-for-dev

## Story

As a **company Member**, I want real-time feedback on allocation rules, so that I know when my allocation is valid.

## Acceptance Criteria

1. **Given** I am editing a draft, **When** I adjust amounts, **Then** UI updates: BudgetBar, percentages, GuardrailBadge status.

2. **Given** I allocate to fewer than 3 packages, **Then** I see "⚠️ Minimum 3 packages required".

3. **Given** I allocate more than 34% to one package, **Then** I see "⚠️ Max 34% per package exceeded".

4. **Given** all guardrails satisfied, **Then** I see "✅ Allocation valid".

## Tasks

- [ ] Create `GuardrailValidator` domain service (MIN_PACKAGES=3, MAX_SHARE_PERCENT=34)
- [ ] Create `UpdateAllocationDraftUseCase` with debounced auto-save
- [ ] Create GuardrailBadge component
- [ ] Implement real-time validation in frontend

## Dev Notes

### GuardrailValidator
```java
public class GuardrailValidator {
    public static final int MIN_PACKAGES = 3;
    public static final BigDecimal MAX_SHARE_PERCENT = new BigDecimal("34");

    public ValidationResult validate(AllocationDraft draft) {
        List<String> violations = new ArrayList<>();
        
        int allocatedPackages = (int) draft.getLineItems().stream()
            .filter(li -> li.getAmountCents() > 0)
            .count();
        
        if (allocatedPackages < MIN_PACKAGES) {
            violations.add("Minimum " + MIN_PACKAGES + " packages required");
        }

        for (AllocationLineItem item : draft.getLineItems()) {
            if (item.getPercentage().compareTo(MAX_SHARE_PERCENT) > 0) {
                violations.add("Max " + MAX_SHARE_PERCENT + "% per package exceeded");
                break;
            }
        }

        return new ValidationResult(violations.isEmpty(), violations);
    }
}
```

### GuardrailBadge Component
```tsx
interface GuardrailBadgeProps {
  rule: string
  satisfied: boolean
}

export function GuardrailBadge({ rule, satisfied }: GuardrailBadgeProps) {
  return (
    <div className={cn(
      "flex items-center gap-2 px-3 py-1.5 rounded-full text-sm",
      satisfied ? "bg-success/10 text-success" : "bg-warning/10 text-warning"
    )}>
      {satisfied ? <CheckIcon className="h-4 w-4" /> : <AlertTriangleIcon className="h-4 w-4" />}
      {rule}
    </div>
  )
}
```


### References
- [Source: architecture.md#Communication-State-Machine-Patterns] - Guardrails
- [Source: epics.md#Story-4.2]
- FR13: Allocation guardrails

## Dev Agent Record
### Agent Model Used
_To be filled_

