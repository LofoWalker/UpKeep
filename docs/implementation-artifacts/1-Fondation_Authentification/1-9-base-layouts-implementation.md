# Story 1.9: Base Layouts Implementation

Status: completed

## Story

As a **developer**,
I want pre-built layout templates,
so that I can quickly scaffold new pages.

## Acceptance Criteria

1. **Given** I am creating a new page  
   **When** I use `OnboardingLayout`  
   **Then** I get a centered card layout with progress stepper slot

2. **Given** I am creating an authenticated page  
   **When** I use `DashboardLayout`  
   **Then** I get a layout with:
   - Top navbar with logo, workspace switcher slot, user menu
   - Optional tab navigation
   - Main content area

3. **Given** I am creating the public sponsorship page  
   **When** I use `PublicPageLayout`  
   **Then** I get a layout with:
   - Minimal header with Upkeep branding
   - Hero section slot
   - Content area

4. **Given** I use any layout  
   **When** I resize the browser  
   **Then** all layouts are responsive (desktop-first, tablet/mobile friendly)

5. **Given** I use any layout  
   **When** the page is loading or errors  
   **Then** layouts handle loading and error states

## Tasks / Subtasks

- [x] Task 1: Create OnboardingLayout (AC: #1, #4, #5)
  - [x] 1.1: Create OnboardingLayout component
  - [x] 1.2: Create ProgressStepper component
  - [x] 1.3: Add responsive styles
  - [x] 1.4: Add loading state support

- [x] Task 2: Create DashboardLayout (AC: #2, #4, #5)
  - [x] 2.1: Create DashboardLayout component
  - [x] 2.2: Create Navbar component
  - [x] 2.3: Create WorkspaceSwitcher placeholder
  - [x] 2.4: Create UserMenu component
  - [x] 2.5: Create TabNav component
  - [x] 2.6: Add responsive sidebar for mobile

- [x] Task 3: Create PublicPageLayout (AC: #3, #4)
  - [x] 3.1: Create PublicPageLayout component
  - [x] 3.2: Create PublicHeader component
  - [x] 3.3: Create Footer component

- [x] Task 4: Create AdminLayout (bonus)
  - [x] 4.1: Create AdminLayout component
  - [x] 4.2: Create AdminSidebar component

- [x] Task 5: Create shared layout utilities (AC: #5)
  - [x] 5.1: Create PageLoading component
  - [x] 5.2: Create PageError component
  - [x] 5.3: Create ErrorBoundary wrapper (reusing existing)

## Dev Notes

### Layout Directory Structure

```
apps/web/src/components/layout/
├── OnboardingLayout.tsx
├── ProgressStepper.tsx
├── DashboardLayout.tsx
├── Navbar.tsx
├── WorkspaceSwitcher.tsx
├── UserMenu.tsx
├── TabNav.tsx
├── PublicPageLayout.tsx
├── PublicHeader.tsx
├── Footer.tsx
├── AdminLayout.tsx
├── AdminSidebar.tsx
├── PageLoading.tsx
├── PageError.tsx
└── index.ts
```

### OnboardingLayout

```tsx
// apps/web/src/components/layout/OnboardingLayout.tsx
import { ProgressStepper, Step } from './ProgressStepper'

interface OnboardingLayoutProps {
  children: React.ReactNode
  currentStep: number
  steps: Step[]
  title?: string
}

export function OnboardingLayout({
  children,
  currentStep,
  steps,
  title,
}: OnboardingLayoutProps) {
  return (
    <div className="min-h-screen bg-muted/30 flex flex-col">
      {/* Header */}
      <header className="border-b bg-background px-4 py-3">
        <div className="mx-auto max-w-2xl flex items-center justify-center">
          <Logo className="h-8" />
        </div>
      </header>

      {/* Progress */}
      <div className="border-b bg-background px-4 py-4">
        <div className="mx-auto max-w-2xl">
          <ProgressStepper steps={steps} currentStep={currentStep} />
        </div>
      </div>

      {/* Content */}
      <main className="flex-1 px-4 py-8">
        <div className="mx-auto max-w-2xl">
          <Card>
            <CardHeader>
              {title && <CardTitle>{title}</CardTitle>}
            </CardHeader>
            <CardContent>{children}</CardContent>
          </Card>
        </div>
      </main>
    </div>
  )
}
```

### ProgressStepper

```tsx
// apps/web/src/components/layout/ProgressStepper.tsx
export interface Step {
  id: string
  label: string
  description?: string
}

interface ProgressStepperProps {
  steps: Step[]
  currentStep: number
}

export function ProgressStepper({ steps, currentStep }: ProgressStepperProps) {
  return (
    <nav aria-label="Progress">
      <ol className="flex items-center justify-between">
        {steps.map((step, index) => (
          <li key={step.id} className="flex items-center">
            <div className="flex flex-col items-center">
              <div
                className={cn(
                  'flex h-8 w-8 items-center justify-center rounded-full border-2',
                  index < currentStep && 'border-primary bg-primary text-primary-foreground',
                  index === currentStep && 'border-primary text-primary',
                  index > currentStep && 'border-muted text-muted-foreground'
                )}
              >
                {index < currentStep ? (
                  <CheckIcon className="h-4 w-4" />
                ) : (
                  <span>{index + 1}</span>
                )}
              </div>
              <span className="mt-2 text-xs font-medium">{step.label}</span>
            </div>
            {index < steps.length - 1 && (
              <div
                className={cn(
                  'mx-4 h-0.5 w-12 sm:w-24',
                  index < currentStep ? 'bg-primary' : 'bg-muted'
                )}
              />
            )}
          </li>
        ))}
      </ol>
    </nav>
  )
}
```

### DashboardLayout

```tsx
// apps/web/src/components/layout/DashboardLayout.tsx
interface DashboardLayoutProps {
  children: React.ReactNode
  tabs?: TabItem[]
  activeTab?: string
}

export function DashboardLayout({ children, tabs, activeTab }: DashboardLayoutProps) {
  return (
    <div className="min-h-screen bg-muted/30">
      <Navbar />
      
      {tabs && tabs.length > 0 && (
        <div className="border-b bg-background">
          <div className="mx-auto max-w-7xl px-4">
            <TabNav tabs={tabs} activeTab={activeTab} />
          </div>
        </div>
      )}

      <main className="mx-auto max-w-7xl px-4 py-6">
        {children}
      </main>
    </div>
  )
}
```

### Navbar

```tsx
// apps/web/src/components/layout/Navbar.tsx
export function Navbar() {
  return (
    <header className="border-b bg-background">
      <div className="mx-auto max-w-7xl px-4">
        <div className="flex h-16 items-center justify-between">
          {/* Logo */}
          <div className="flex items-center gap-4">
            <Logo className="h-8" />
            <WorkspaceSwitcher />
          </div>

          {/* Right side */}
          <div className="flex items-center gap-4">
            <UserMenu />
          </div>
        </div>
      </div>
    </header>
  )
}
```

### WorkspaceSwitcher (Placeholder)

```tsx
// apps/web/src/components/layout/WorkspaceSwitcher.tsx
export function WorkspaceSwitcher() {
  const { currentCompany, companies } = useCompanyContext()

  if (!currentCompany) return null

  if (companies.length === 1) {
    return (
      <span className="text-sm font-medium">{currentCompany.name}</span>
    )
  }

  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <Button variant="ghost" className="gap-2">
          {currentCompany.name}
          <ChevronDownIcon className="h-4 w-4" />
        </Button>
      </DropdownMenuTrigger>
      <DropdownMenuContent align="start">
        {companies.map((company) => (
          <DropdownMenuItem key={company.id}>
            {company.name}
            {company.id === currentCompany.id && (
              <CheckIcon className="ml-auto h-4 w-4" />
            )}
          </DropdownMenuItem>
        ))}
      </DropdownMenuContent>
    </DropdownMenu>
  )
}
```

### UserMenu

```tsx
// apps/web/src/components/layout/UserMenu.tsx
export function UserMenu() {
  const { user, logout } = useAuth()

  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <Button variant="ghost" size="icon" className="rounded-full">
          <Avatar>
            <AvatarImage src={user?.avatarUrl} />
            <AvatarFallback>{user?.email?.charAt(0).toUpperCase()}</AvatarFallback>
          </Avatar>
        </Button>
      </DropdownMenuTrigger>
      <DropdownMenuContent align="end">
        <DropdownMenuLabel>{user?.email}</DropdownMenuLabel>
        <DropdownMenuSeparator />
        <DropdownMenuItem>Settings</DropdownMenuItem>
        <DropdownMenuItem onClick={logout}>Log out</DropdownMenuItem>
      </DropdownMenuContent>
    </DropdownMenu>
  )
}
```

### TabNav

```tsx
// apps/web/src/components/layout/TabNav.tsx
export interface TabItem {
  id: string
  label: string
  href: string
  icon?: React.ComponentType<{ className?: string }>
}

interface TabNavProps {
  tabs: TabItem[]
  activeTab?: string
}

export function TabNav({ tabs, activeTab }: TabNavProps) {
  return (
    <nav className="flex space-x-8">
      {tabs.map((tab) => (
        <Link
          key={tab.id}
          to={tab.href}
          className={cn(
            'border-b-2 px-1 py-4 text-sm font-medium',
            activeTab === tab.id
              ? 'border-primary text-primary'
              : 'border-transparent text-muted-foreground hover:border-muted hover:text-foreground'
          )}
        >
          {tab.icon && <tab.icon className="mr-2 h-4 w-4 inline" />}
          {tab.label}
        </Link>
      ))}
    </nav>
  )
}
```

### PublicPageLayout

```tsx
// apps/web/src/components/layout/PublicPageLayout.tsx
interface PublicPageLayoutProps {
  children: React.ReactNode
  hero?: React.ReactNode
}

export function PublicPageLayout({ children, hero }: PublicPageLayoutProps) {
  return (
    <div className="min-h-screen flex flex-col">
      <PublicHeader />
      
      {hero && (
        <div className="bg-gradient-to-b from-primary/5 to-background">
          {hero}
        </div>
      )}

      <main className="flex-1">
        {children}
      </main>

      <Footer />
    </div>
  )
}
```

### PageLoading & PageError

```tsx
// apps/web/src/components/layout/PageLoading.tsx
export function PageLoading({ message = 'Loading...' }: { message?: string }) {
  return (
    <div className="flex min-h-[400px] items-center justify-center">
      <LoadingSpinner size="lg" message={message} />
    </div>
  )
}

// apps/web/src/components/layout/PageError.tsx
interface PageErrorProps {
  title?: string
  message?: string
  retry?: () => void
}

export function PageError({ 
  title = 'Something went wrong',
  message = 'An unexpected error occurred',
  retry 
}: PageErrorProps) {
  return (
    <div className="flex min-h-[400px] flex-col items-center justify-center gap-4">
      <AlertCircleIcon className="h-12 w-12 text-error" />
      <h2 className="text-lg font-semibold">{title}</h2>
      <p className="text-muted-foreground">{message}</p>
      {retry && (
        <Button onClick={retry} variant="outline">
          Try again
        </Button>
      )}
    </div>
  )
}
```

### Responsive Breakpoints

```
Mobile: < 640px (default)
Tablet: sm (640px+)
Desktop: md (768px+)
Large: lg (1024px+)
XL: xl (1280px+)
```

### Usage Examples

```tsx
// Onboarding page
function CreateWorkspacePage() {
  const steps = [
    { id: 'workspace', label: 'Workspace' },
    { id: 'budget', label: 'Budget' },
    { id: 'dependencies', label: 'Dependencies' },
    { id: 'allocate', label: 'Allocate' },
  ]

  return (
    <OnboardingLayout steps={steps} currentStep={0} title="Create Workspace">
      <CreateWorkspaceForm />
    </OnboardingLayout>
  )
}

// Dashboard page
function DashboardPage() {
  const tabs = [
    { id: 'overview', label: 'Overview', href: '/dashboard' },
    { id: 'packages', label: 'Packages', href: '/dashboard/packages' },
    { id: 'allocations', label: 'Allocations', href: '/dashboard/allocations' },
    { id: 'settings', label: 'Settings', href: '/dashboard/settings' },
  ]

  return (
    <DashboardLayout tabs={tabs} activeTab="overview">
      <DashboardContent />
    </DashboardLayout>
  )
}
```

### Dependencies on Previous Stories

- Story 1.8: Design system and base components

### References

- [Source: architecture.md#Frontend-Architecture] - Layout templates
- [Source: architecture.md#UX-Flows-Screen-Architecture] - Screen inventory
- [Source: epics.md#Story-1.9] - Original acceptance criteria

## Dev Agent Record

### Agent Model Used

Claude (Anthropic)

### Completion Notes List

- Implemented all layout components as specified in dev notes
- All layouts are responsive (mobile-first approach with Tailwind breakpoints)
- OnboardingLayout includes progress stepper with step completion indicators
- DashboardLayout includes navbar with workspace switcher and user menu
- AdminLayout includes collapsible sidebar navigation
- Created PageLoading and PageError utility components for state handling
- Reused existing ErrorBoundary from common components
- Added Storybook stories for key components (ProgressStepper, TabNav, WorkspaceSwitcher, PageLoading, PageError)
- All components pass TypeScript compilation without errors

### Change Log

- Created `/apps/web/src/components/layout/Logo.tsx` - Reusable logo component with optional link
- Created `/apps/web/src/components/layout/ProgressStepper.tsx` - Step indicator for onboarding flows
- Created `/apps/web/src/components/layout/OnboardingLayout.tsx` - Centered card layout with progress stepper
- Created `/apps/web/src/components/layout/TabNav.tsx` - Horizontal tab navigation
- Created `/apps/web/src/components/layout/WorkspaceSwitcher.tsx` - Dropdown for switching between workspaces
- Created `/apps/web/src/components/layout/UserMenu.tsx` - User avatar dropdown with settings and logout
- Created `/apps/web/src/components/layout/Navbar.tsx` - Top navigation bar for dashboard
- Created `/apps/web/src/components/layout/DashboardLayout.tsx` - Main authenticated page layout
- Created `/apps/web/src/components/layout/PublicHeader.tsx` - Minimal header for public pages
- Created `/apps/web/src/components/layout/Footer.tsx` - Site footer with links
- Created `/apps/web/src/components/layout/PublicPageLayout.tsx` - Layout for public-facing pages
- Created `/apps/web/src/components/layout/PageLoading.tsx` - Loading state component
- Created `/apps/web/src/components/layout/PageError.tsx` - Error state component with retry option
- Created `/apps/web/src/components/layout/AdminLayout.tsx` - Admin panel layout with sidebar
- Created `/apps/web/src/components/layout/index.ts` - Barrel export for all layout components
- Created Storybook stories for ProgressStepper, TabNav, WorkspaceSwitcher, PageLoading, PageError

### File List

```
apps/web/src/components/layout/
├── AdminLayout.tsx
├── DashboardLayout.tsx
├── Footer.tsx
├── index.ts
├── Logo.tsx
├── Navbar.tsx
├── OnboardingLayout.tsx
├── PageError.stories.tsx
├── PageError.tsx
├── PageLoading.stories.tsx
├── PageLoading.tsx
├── ProgressStepper.stories.tsx
├── ProgressStepper.tsx
├── PublicHeader.tsx
├── PublicPageLayout.tsx
├── TabNav.stories.tsx
├── TabNav.tsx
├── UserMenu.tsx
├── WorkspaceSwitcher.stories.tsx
└── WorkspaceSwitcher.tsx
```

