# Story 3.5: View Package List

Status: ready-for-dev

## Story

As a **company Member**, I want to view all imported packages, so that I can see what dependencies my company tracks.

## Acceptance Criteria

1. **Given** packages have been imported, **When** I go to Packages page, **Then** I see PackageCard components showing name, allocation (if any), claim status.

2. **Given** many packages, **When** I use search, **Then** packages are filtered by name in real-time.

3. **Given** no packages exist, **When** I view the page, **Then** I see EmptyState with CTA to import.

## Tasks

- [ ] Create `ListCompanyPackagesUseCase` with pagination
- [ ] Create PackageCard component
- [ ] Implement real-time search filter
- [ ] Add infinite scroll (50 packages per page)

## Dev Notes

### PackageCard Component
```tsx
interface PackageCardProps {
  name: string
  allocationCents?: number
  claimStatus: 'claimed' | 'unclaimed'
  currency?: string
}

export function PackageCard({ name, allocationCents, claimStatus, currency }: PackageCardProps) {
  return (
    <Card className="p-4">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-3">
          <PackageIcon className="h-8 w-8 text-muted-foreground" />
          <div>
            <p className="font-medium">{name}</p>
            <Badge variant={claimStatus === 'claimed' ? 'success' : 'secondary'}>
              {claimStatus}
            </Badge>
          </div>
        </div>
        {allocationCents && (
          <p className="font-semibold">{formatCurrency(allocationCents, currency)}</p>
        )}
      </div>
    </Card>
  )
}
```


### References
- [Source: epics.md#Story-3.5]

## Dev Agent Record
### Agent Model Used
_To be filled_

