# Story 2.2: Company Dashboard Shell

Status: ready-for-dev

## Story

As a **company member**,
I want to access my company dashboard,
so that I can see an overview and navigate to features.

## Acceptance Criteria

1. **Given** I am authenticated and belong to a company  
   **When** I navigate to the dashboard  
   **Then** I see:
   - Company name in the header
   - Navigation tabs (Overview, Packages, Allocations, Settings)
   - Empty states with CTAs for features not yet set up  
   **And** the URL reflects my company: `/dashboard`

2. **Given** I have no company yet  
   **When** I try to access the dashboard  
   **Then** I am redirected to company creation flow

3. **Given** I am on the dashboard  
   **When** I view the Overview tab  
   **Then** I see placeholder cards for KPIs (to be implemented)

## Tasks / Subtasks

- [ ] Task 1: Create dashboard use case (AC: #1, #2)
  - [ ] 1.1: Create `GetCompanyDashboardUseCase` port
  - [ ] 1.2: Implement dashboard data retrieval
  - [ ] 1.3: Create company context/provider

- [ ] Task 2: Create REST endpoint (AC: #1)
  - [ ] 2.1: Create `/api/companies/me` endpoint
  - [ ] 2.2: Create `/api/companies/{id}/dashboard` endpoint
  - [ ] 2.3: Add tenant scoping

- [ ] Task 3: Create frontend dashboard (AC: #1, #2, #3)
  - [ ] 3.1: Create dashboard page with DashboardLayout
  - [ ] 3.2: Create Overview tab content
  - [ ] 3.3: Create empty state components
  - [ ] 3.4: Add redirect logic for users without company

- [ ] Task 4: Setup routing (AC: #1, #2)
  - [ ] 4.1: Configure React Router for dashboard routes
  - [ ] 4.2: Create protected route wrapper
  - [ ] 4.3: Create company required wrapper

## Dev Notes

### Use Case

```java
package com.upkeep.application.port.in;

public interface GetCompanyDashboardUseCase {
    DashboardData execute(String userId);

    record DashboardData(
        CompanyInfo company,
        DashboardStats stats
    ) {}

    record CompanyInfo(
        String id,
        String name,
        String slug,
        String role
    ) {}

    record DashboardStats(
        int totalPackages,
        int allocatedPackages,
        Long monthlyBudgetCents,
        Long allocatedAmountCents,
        String currentPeriod
    ) {}
}
```

### REST Endpoints

```java
@Path("/api/companies")
@Authenticated
public class CompanyResource {

    @GET
    @Path("/me")
    public Response getCurrentUserCompanies(@Context SecurityContext ctx) {
        String userId = ctx.getUserPrincipal().getName();
        List<CompanyInfo> companies = getUserCompaniesUseCase.execute(userId);
        
        return Response.ok(ApiResponse.success(companies)).build();
    }

    @GET
    @Path("/{companyId}/dashboard")
    public Response getDashboard(
        @PathParam("companyId") String companyId,
        @Context SecurityContext ctx
    ) {
        String userId = ctx.getUserPrincipal().getName();
        
        // Verify membership
        verifyMembership(userId, companyId);
        
        DashboardData data = getCompanyDashboardUseCase.execute(companyId);
        return Response.ok(ApiResponse.success(data)).build();
    }
}
```

### Frontend Dashboard Page

```tsx
// apps/web/src/pages/dashboard/index.tsx
export function DashboardPage() {
  const { currentCompany } = useCompanyContext()
  const { data, isLoading, error } = useQuery({
    queryKey: ['dashboard', currentCompany?.id],
    queryFn: () => api.get(`/companies/${currentCompany?.id}/dashboard`),
    enabled: !!currentCompany,
  })

  if (isLoading) return <PageLoading />
  if (error) return <PageError retry={() => refetch()} />

  const tabs = [
    { id: 'overview', label: 'Overview', href: '/dashboard' },
    { id: 'packages', label: 'Packages', href: '/dashboard/packages' },
    { id: 'allocations', label: 'Allocations', href: '/dashboard/allocations' },
    { id: 'settings', label: 'Settings', href: '/dashboard/settings' },
  ]

  return (
    <DashboardLayout tabs={tabs} activeTab="overview">
      <DashboardOverview stats={data?.stats} />
    </DashboardLayout>
  )
}
```

### Dashboard Overview Component

```tsx
// apps/web/src/features/company/components/DashboardOverview.tsx
export function DashboardOverview({ stats }: { stats: DashboardStats }) {
  const hasSetup = stats.monthlyBudgetCents != null

  if (!hasSetup) {
    return (
      <EmptyState
        icon={<RocketIcon />}
        title="Welcome to Upkeep!"
        description="Get started by setting your monthly budget and importing your dependencies."
        action={
          <Button asChild>
            <Link to="/onboarding/budget">Set Up Budget</Link>
          </Button>
        }
      />
    )
  }

  return (
    <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
      <StatsCard
        title="Monthly Budget"
        value={formatCurrency(stats.monthlyBudgetCents)}
        icon={<WalletIcon />}
      />
      <StatsCard
        title="Allocated"
        value={formatCurrency(stats.allocatedAmountCents)}
        description={`${stats.allocatedPackages} packages`}
        icon={<PieChartIcon />}
      />
      <StatsCard
        title="Total Packages"
        value={stats.totalPackages}
        icon={<PackageIcon />}
      />
      <StatsCard
        title="Current Period"
        value={stats.currentPeriod}
        icon={<CalendarIcon />}
      />
    </div>
  )
}
```

### StatsCard Component

```tsx
// apps/web/src/components/common/StatsCard.tsx
interface StatsCardProps {
  title: string
  value: string | number
  description?: string
  icon?: React.ReactNode
  trend?: { value: number; label: string }
}

export function StatsCard({ title, value, description, icon, trend }: StatsCardProps) {
  return (
    <Card>
      <CardHeader className="flex flex-row items-center justify-between pb-2">
        <CardTitle className="text-sm font-medium text-muted-foreground">
          {title}
        </CardTitle>
        {icon && <div className="text-muted-foreground">{icon}</div>}
      </CardHeader>
      <CardContent>
        <div className="text-2xl font-bold">{value}</div>
        {description && (
          <p className="text-xs text-muted-foreground">{description}</p>
        )}
        {trend && (
          <p className={cn(
            'text-xs',
            trend.value > 0 ? 'text-success' : 'text-error'
          )}>
            {trend.value > 0 ? '+' : ''}{trend.value}% {trend.label}
          </p>
        )}
      </CardContent>
    </Card>
  )
}
```

### EmptyState Component

```tsx
// apps/web/src/components/common/EmptyState.tsx
interface EmptyStateProps {
  icon?: React.ReactNode
  title: string
  description: string
  action?: React.ReactNode
}

export function EmptyState({ icon, title, description, action }: EmptyStateProps) {
  return (
    <div className="flex flex-col items-center justify-center py-12 text-center">
      {icon && (
        <div className="mb-4 rounded-full bg-muted p-3">
          {icon}
        </div>
      )}
      <h3 className="text-lg font-semibold">{title}</h3>
      <p className="mt-1 text-sm text-muted-foreground max-w-md">{description}</p>
      {action && <div className="mt-4">{action}</div>}
    </div>
  )
}
```

### Company Context

```tsx
// apps/web/src/features/company/context/CompanyContext.tsx
interface CompanyContextType {
  companies: Company[]
  currentCompany: Company | null
  setCurrentCompany: (company: Company) => void
  isLoading: boolean
}

export function CompanyProvider({ children }: { children: React.ReactNode }) {
  const { user } = useAuth()
  const [currentCompany, setCurrentCompany] = useState<Company | null>(null)

  const { data: companies, isLoading } = useQuery({
    queryKey: ['companies', user?.id],
    queryFn: () => api.get('/companies/me'),
    enabled: !!user,
  })

  useEffect(() => {
    if (companies?.length && !currentCompany) {
      setCurrentCompany(companies[0])
    }
  }, [companies])

  return (
    <CompanyContext.Provider value={{ companies, currentCompany, setCurrentCompany, isLoading }}>
      {children}
    </CompanyContext.Provider>
  )
}
```

### Routing Configuration

```tsx
// apps/web/src/routes/index.tsx
export function AppRoutes() {
  return (
    <Routes>
      {/* Public routes */}
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />
      
      {/* Protected routes */}
      <Route element={<ProtectedRoute />}>
        {/* Requires company */}
        <Route element={<RequireCompany />}>
          <Route path="/dashboard" element={<DashboardPage />} />
          <Route path="/dashboard/packages" element={<PackagesPage />} />
          <Route path="/dashboard/allocations" element={<AllocationsPage />} />
          <Route path="/dashboard/settings" element={<SettingsPage />} />
        </Route>
        
        {/* Company creation */}
        <Route path="/onboarding/*" element={<OnboardingRoutes />} />
      </Route>
    </Routes>
  )
}

function RequireCompany({ children }: { children?: React.ReactNode }) {
  const { currentCompany, isLoading } = useCompanyContext()
  
  if (isLoading) return <PageLoading />
  if (!currentCompany) return <Navigate to="/onboarding/workspace" replace />
  
  return children ?? <Outlet />
}
```

### Dependencies on Previous Stories

- Story 1.9: DashboardLayout
- Story 2.1: Company creation

### E2E Testing Requirements

**Required Playwright tests for this story:**

- [ ] Create Page Object Model for dashboard (`e2e/pages/dashboard.ts`)
- [ ] Test: Authenticated user with company sees dashboard with navigation tabs
- [ ] Test: User without company is redirected to onboarding
- [ ] Test: Overview tab displays placeholder KPI cards
- [ ] Test: Navigation between dashboard tabs works correctly

**Test file location:** `apps/web/e2e/dashboard.spec.ts`

### References

- [Source: architecture.md#UX-Flows-Screen-Architecture] - Dashboard screens
- [Source: epics.md#Story-2.2] - Original acceptance criteria

## Dev Agent Record

### Agent Model Used

_To be filled by dev agent_

### Completion Notes List

_To be filled during implementation_

### Change Log

_To be filled during implementation_

### File List

_To be filled after implementation_

