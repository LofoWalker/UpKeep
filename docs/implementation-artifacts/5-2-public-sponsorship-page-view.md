# Story 5.2: Public Sponsorship Page View

Status: ready-for-dev

## Story

As a **visitor**, I want to view a company's public sponsorship page, so that I can see their open-source commitment.

## Acceptance Criteria

1. **Given** page is enabled, **When** I visit `/sponsors/[slug]`, **Then** I see: company name/logo, "Proudly supporting open source", funded packages list, aggregate monthly amount, total packages count.

2. **Given** no finalized allocations, **When** I view page, **Then** I see "No active sponsorships yet".

## Tasks

- [ ] Create `GetPublicSponsorshipPageUseCase` (no auth required)
- [ ] Create public page with PublicPageLayout
- [ ] Aggregate data from latest AllocationSnapshot
- [ ] Style with hero section

## Dev Notes

### Public Page UI
```tsx
export function PublicSponsorshipPage() {
  const { slug } = useParams()
  const { data, isLoading, error } = useQuery({
    queryKey: ['public-sponsorship', slug],
    queryFn: () => api.get(`/public/companies/${slug}`),
  })

  if (error?.status === 404) return <NotFoundPage />

  return (
    <PublicPageLayout 
      hero={
        <div className="py-16 text-center">
          <Avatar className="h-24 w-24 mx-auto">
            <AvatarFallback className="text-2xl">{data.name[0]}</AvatarFallback>
          </Avatar>
          <h1 className="mt-4 text-3xl font-bold">{data.name}</h1>
          <p className="text-xl text-muted-foreground">Proudly supporting open source</p>
        </div>
      }
    >
      <div className="max-w-4xl mx-auto py-12">
        <div className="grid grid-cols-2 gap-8 mb-12 text-center">
          <StatsCard title="Monthly Sponsorship" value={formatCurrency(data.monthlyTotal)} />
          <StatsCard title="Packages Funded" value={data.packageCount} />
        </div>
        <h2 className="text-xl font-semibold mb-4">Funded Packages</h2>
        <div className="grid gap-2">
          {data.packages.map(pkg => (
            <div key={pkg.name} className="flex items-center gap-2 p-3 bg-muted rounded">
              <PackageIcon className="h-4 w-4" />
              <span>{pkg.name}</span>
            </div>
          ))}
        </div>
      </div>
    </PublicPageLayout>
  )
}
```

### E2E Testing Requirements

**Required Playwright tests for this story:**

- [ ] Create Page Object Model for public sponsorship page (`e2e/pages/public-sponsorship.ts`)
- [ ] Test: Public page displays company name and aggregate stats
- [ ] Test: Sponsored packages list shows correctly
- [ ] Test: Unauthenticated users can view public page

**Test file location:** `apps/web/e2e/public-sponsorship.spec.ts`

### References
- [Source: epics.md#Story-5.2]
- FR18, FR19: View public page with aggregates

## Dev Agent Record
### Agent Model Used
_To be filled_

