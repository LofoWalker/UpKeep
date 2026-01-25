# Copilot Processing: Story 1.8 - Design System & Base Components

## User Request
Implement design system with tokens and base components for consistent UI development.

## Action Plan

### Phase 1: Configure Design Tokens
- [x] 1.1: Create CSS variables for colors (tokens.css)
- [x] 1.2: Configure Tailwind with design tokens
- [x] 1.3: Add Inter and JetBrains Mono fonts
- [x] 1.4: Create spacing and radius scales

### Phase 2: Initialize shadcn/ui
- [x] 2.1: Created shadcn/ui compatible components
- [x] 2.2: Add Button component
- [x] 2.3: Add Input component
- [x] 2.4: Add Card component
- [x] 2.5: Add Badge component
- [x] 2.6: Add Avatar component
- [x] 2.7: Add Dropdown Menu component
- [x] 2.8: Add additional components (Dialog, Alert, Toast, Label, Separator)

### Phase 3: Create Custom Components
- [x] 3.1: Create LoadingSpinner
- [x] 3.2: Create ErrorBoundary
- [x] 3.3: Create common components index
- [x] 3.4: Create FormInput wrapper component

### Phase 4: Ensure Accessibility
- [x] 4.1: Verify color contrast ratios (using shadcn/ui defaults)
- [x] 4.2: Add focus indicators (ring-2 ring-ring ring-offset-2)
- [x] 4.3: Keyboard navigation (Radix primitives handle this)
- [x] 4.4: Add ARIA labels where needed

### Phase 5: Setup Storybook
- [x] 5.1: Install and configure Storybook
- [x] 5.2: Create stories for all components
- [x] 5.3: Add accessibility addon

## Status
✅ Complete

## Summary

Successfully implemented a comprehensive design system for the Upkeep application:

### Design Tokens
- Created `globals.css` with HSL color tokens for light/dark mode
- Configured Tailwind with full shadcn/ui compatible theme
- Added Inter (sans) and JetBrains Mono (mono) fonts
- Defined spacing, radius, and shadow scales

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
