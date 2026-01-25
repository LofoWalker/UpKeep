# Copilot Processing: Story 1.9 - Base Layouts Implementation

## User Request
Implement base layouts for the Upkeep application (OnboardingLayout, DashboardLayout, PublicPageLayout, AdminLayout).

## Action Plan

### Phase 1: Create OnboardingLayout
- [x] 1.1: Create Logo component
- [x] 1.2: Create ProgressStepper component
- [x] 1.3: Create OnboardingLayout component
- [x] 1.4: Add responsive styles

### Phase 2: Create DashboardLayout
- [x] 2.1: Create TabNav component
- [x] 2.2: Create WorkspaceSwitcher component
- [x] 2.3: Create UserMenu component
- [x] 2.4: Create Navbar component
- [x] 2.5: Create DashboardLayout component
- [x] 2.6: Add mobile responsive menu

### Phase 3: Create PublicPageLayout
- [x] 3.1: Create PublicHeader component
- [x] 3.2: Create Footer component
- [x] 3.3: Create PublicPageLayout component

### Phase 4: Create AdminLayout
- [x] 4.1: Create AdminSidebar (integrated in AdminLayout)
- [x] 4.2: Create AdminLayout component

### Phase 5: Create shared utilities
- [x] 5.1: Create PageLoading component
- [x] 5.2: Create PageError component
- [x] 5.3: Create index.ts barrel export
- [x] 5.4: Add Storybook stories for key components

## Status
✅ Complete

## Summary

Successfully implemented all base layouts for the Upkeep application:

### Layout Components Created
- **Logo** - Reusable brand logo with optional link
- **ProgressStepper** - Step indicator for multi-step flows
- **OnboardingLayout** - Centered card layout with progress tracking
- **TabNav** - Horizontal tab navigation with icons support
- **WorkspaceSwitcher** - Dropdown for switching workspaces/companies
- **UserMenu** - Avatar dropdown with settings and logout
- **Navbar** - Top navigation bar with mobile menu support
- **DashboardLayout** - Main authenticated page layout
- **PublicHeader** - Minimal header for public pages
- **Footer** - Site footer with links
- **PublicPageLayout** - Layout for public-facing pages
- **AdminLayout** - Admin panel with collapsible sidebar
- **PageLoading** - Loading state component
- **PageError** - Error state with retry option

### Storybook Stories
- ProgressStepper.stories.tsx
- TabNav.stories.tsx
- WorkspaceSwitcher.stories.tsx
- PageLoading.stories.tsx
- PageError.stories.tsx

### Files Created
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

**Note:** Please review and remove this file when done.
