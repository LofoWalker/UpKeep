# Story 2.6: Workspace Switcher

Status: ready-for-dev

## Story

As a **user belonging to multiple companies**,
I want to switch between my company workspaces,
so that I can manage funding for different organizations.

## Acceptance Criteria

1. **Given** I belong to multiple companies  
   **When** I click on the workspace switcher in the navbar  
   **Then** I see a dropdown listing all my companies with:
   - Company name
   - My role in each
   - Visual indicator for current workspace

2. **Given** I select a different company  
   **When** the selection is made  
   **Then** the dashboard reloads with that company's data  
   **And** the URL context updates

3. **Given** I belong to only one company  
   **When** I view the navbar  
   **Then** the switcher shows my company name without dropdown

## Tasks / Subtasks

- [ ] Task 1: Create use case (AC: #1)
  - [ ] 1.1: Create `GetUserCompaniesUseCase`
  - [ ] 1.2: Return companies with roles

- [ ] Task 2: Update company context (AC: #1, #2)
  - [ ] 2.1: Store current company in context
  - [ ] 2.2: Persist selection in localStorage
  - [ ] 2.3: Handle company switching

- [ ] Task 3: Create frontend component (AC: #1, #2, #3)
  - [ ] 3.1: Implement WorkspaceSwitcher dropdown
  - [ ] 3.2: Add single company display
  - [ ] 3.3: Handle data refresh on switch

## Dev Notes

### Use Case

```java
public interface GetUserCompaniesUseCase {
    List<CompanyWithRole> execute(String userId);

    record CompanyWithRole(
        String id,
        String name,
        String slug,
        String role
    ) {}
}
```

### Frontend WorkspaceSwitcher

```tsx
// apps/web/src/components/layout/WorkspaceSwitcher.tsx
export function WorkspaceSwitcher() {
  const { companies, currentCompany, setCurrentCompany } = useCompanyContext()

  if (!currentCompany) return null

  // Single company - no dropdown
  if (companies.length === 1) {
    return (
      <div className="flex items-center gap-2 px-3 py-2">
        <BuildingIcon className="h-4 w-4 text-muted-foreground" />
        <span className="font-medium">{currentCompany.name}</span>
      </div>
    )
  }

  // Multiple companies - dropdown
  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <Button variant="ghost" className="gap-2">
          <BuildingIcon className="h-4 w-4" />
          {currentCompany.name}
          <ChevronDownIcon className="h-4 w-4 text-muted-foreground" />
        </Button>
      </DropdownMenuTrigger>
      <DropdownMenuContent align="start" className="w-64">
        <DropdownMenuLabel>Your Workspaces</DropdownMenuLabel>
        <DropdownMenuSeparator />
        {companies.map((company) => (
          <DropdownMenuItem
            key={company.id}
            onClick={() => setCurrentCompany(company)}
            className="flex items-center justify-between"
          >
            <div className="flex items-center gap-2">
              <Avatar className="h-6 w-6">
                <AvatarFallback className="text-xs">
                  {company.name[0]}
                </AvatarFallback>
              </Avatar>
              <div>
                <p className="font-medium">{company.name}</p>
                <p className="text-xs text-muted-foreground">{company.role}</p>
              </div>
            </div>
            {company.id === currentCompany.id && (
              <CheckIcon className="h-4 w-4 text-primary" />
            )}
          </DropdownMenuItem>
        ))}
        <DropdownMenuSeparator />
        <DropdownMenuItem asChild>
          <Link to="/onboarding/workspace">
            <PlusIcon className="mr-2 h-4 w-4" />
            Create New Workspace
          </Link>
        </DropdownMenuItem>
      </DropdownMenuContent>
    </DropdownMenu>
  )
}
```

### Company Context with Persistence

```tsx
export function CompanyProvider({ children }: { children: React.ReactNode }) {
  const { user } = useAuth()
  const [currentCompanyId, setCurrentCompanyId] = useState<string | null>(() => {
    return localStorage.getItem('currentCompanyId')
  })

  const { data: companies = [], isLoading } = useQuery({
    queryKey: ['companies', user?.id],
    queryFn: () => api.get<CompanyWithRole[]>('/companies/me'),
    enabled: !!user,
  })

  const currentCompany = useMemo(() => {
    if (!companies.length) return null
    return companies.find(c => c.id === currentCompanyId) || companies[0]
  }, [companies, currentCompanyId])

  const setCurrentCompany = useCallback((company: CompanyWithRole) => {
    setCurrentCompanyId(company.id)
    localStorage.setItem('currentCompanyId', company.id)
  }, [])

  // Sync when companies change
  useEffect(() => {
    if (companies.length && !currentCompany) {
      setCurrentCompany(companies[0])
    }
  }, [companies])

  return (
    <CompanyContext.Provider value={{
      companies,
      currentCompany,
      setCurrentCompany,
      isLoading,
      userRole: currentCompany?.role,
    }}>
      {children}
    </CompanyContext.Provider>
  )
}
```

### Dependencies on Previous Stories

- Story 2.1: Company entity
- Story 2.2: Dashboard layout with navbar

### References

- [Source: epics.md#Story-2.6] - Original acceptance criteria

## Dev Agent Record

### Agent Model Used
_To be filled by dev agent_

### Completion Notes List
_To be filled during implementation_

### Change Log
_To be filled during implementation_

### File List
_To be filled after implementation_

