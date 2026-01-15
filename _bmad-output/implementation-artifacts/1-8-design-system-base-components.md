# Story 1.8: Design System & Base Components

Status: ready-for-dev

## Story

As a **developer**,
I want a design system with tokens and base components,
so that I can build consistent UIs.

## Acceptance Criteria

1. **Given** I am building a UI feature  
   **When** I import from the component library  
   **Then** I have access to:
   - Design tokens (colors, spacing, typography, radii, shadows)
   - shadcn/ui components configured with Upkeep theme
   - Base components: Button, Input, Card, Badge, Avatar, Dropdown

2. **Given** I use any component  
   **When** I inspect it for accessibility  
   **Then** all components meet WCAG 2.1 AA contrast requirements (NFR12)

3. **Given** I use any interactive component  
   **When** I navigate with keyboard  
   **Then** components support keyboard navigation

4. **Given** I need to see available components  
   **When** I access the component documentation  
   **Then** a Storybook (or equivalent) documents available components

## Tasks / Subtasks

- [ ] Task 1: Configure design tokens (AC: #1)
  - [ ] 1.1: Create CSS variables for colors
  - [ ] 1.2: Configure Tailwind with design tokens
  - [ ] 1.3: Add Inter and JetBrains Mono fonts
  - [ ] 1.4: Create spacing and radius scales

- [ ] Task 2: Initialize shadcn/ui (AC: #1)
  - [ ] 2.1: Run shadcn/ui init
  - [ ] 2.2: Add Button component
  - [ ] 2.3: Add Input component
  - [ ] 2.4: Add Card component
  - [ ] 2.5: Add Badge component
  - [ ] 2.6: Add Avatar component
  - [ ] 2.7: Add Dropdown Menu component

- [ ] Task 3: Create custom components (AC: #1)
  - [ ] 3.1: Create LoadingSpinner
  - [ ] 3.2: Create Alert component
  - [ ] 3.3: Create Toast/notification system

- [ ] Task 4: Ensure accessibility (AC: #2, #3)
  - [ ] 4.1: Verify color contrast ratios
  - [ ] 4.2: Add focus indicators
  - [ ] 4.3: Test keyboard navigation
  - [ ] 4.4: Add ARIA labels where needed

- [ ] Task 5: Setup Storybook (AC: #4)
  - [ ] 5.1: Install and configure Storybook
  - [ ] 5.2: Create stories for all components
  - [ ] 5.3: Add accessibility addon

## Dev Notes

### Design Tokens (CSS Variables)

```css
/* apps/web/src/styles/tokens.css */
:root {
  /* Colors - Primary */
  --primary: 222.2 47.4% 11.2%;
  --primary-foreground: 210 40% 98%;
  
  /* Colors - Semantic */
  --success: 142.1 76.2% 36.3%;
  --success-foreground: 355.7 100% 97.3%;
  --warning: 38 92% 50%;
  --warning-foreground: 48 96% 89%;
  --error: 0 84.2% 60.2%;
  --error-foreground: 0 0% 98%;
  
  /* Colors - Neutral */
  --background: 0 0% 100%;
  --foreground: 222.2 84% 4.9%;
  --muted: 210 40% 96.1%;
  --muted-foreground: 215.4 16.3% 46.9%;
  --border: 214.3 31.8% 91.4%;
  --input: 214.3 31.8% 91.4%;
  --ring: 222.2 84% 4.9%;
  
  /* Spacing (4px base unit) */
  --space-1: 0.25rem;  /* 4px */
  --space-2: 0.5rem;   /* 8px */
  --space-3: 0.75rem;  /* 12px */
  --space-4: 1rem;     /* 16px */
  --space-5: 1.25rem;  /* 20px */
  --space-6: 1.5rem;   /* 24px */
  --space-8: 2rem;     /* 32px */
  --space-10: 2.5rem;  /* 40px */
  --space-12: 3rem;    /* 48px */
  
  /* Border Radius */
  --radius-sm: 0.25rem;
  --radius-md: 0.375rem;
  --radius-lg: 0.5rem;
  --radius-full: 9999px;
  
  /* Shadows */
  --shadow-sm: 0 1px 2px 0 rgb(0 0 0 / 0.05);
  --shadow-md: 0 4px 6px -1px rgb(0 0 0 / 0.1);
  --shadow-lg: 0 10px 15px -3px rgb(0 0 0 / 0.1);
}

/* Dark mode */
.dark {
  --background: 222.2 84% 4.9%;
  --foreground: 210 40% 98%;
  --muted: 217.2 32.6% 17.5%;
  --muted-foreground: 215 20.2% 65.1%;
  --border: 217.2 32.6% 17.5%;
}
```

### Tailwind Configuration

```typescript
// apps/web/tailwind.config.ts
import type { Config } from 'tailwindcss'

const config: Config = {
  darkMode: ['class'],
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  theme: {
    extend: {
      colors: {
        border: 'hsl(var(--border))',
        input: 'hsl(var(--input))',
        ring: 'hsl(var(--ring))',
        background: 'hsl(var(--background))',
        foreground: 'hsl(var(--foreground))',
        primary: {
          DEFAULT: 'hsl(var(--primary))',
          foreground: 'hsl(var(--primary-foreground))',
        },
        success: {
          DEFAULT: 'hsl(var(--success))',
          foreground: 'hsl(var(--success-foreground))',
        },
        warning: {
          DEFAULT: 'hsl(var(--warning))',
          foreground: 'hsl(var(--warning-foreground))',
        },
        error: {
          DEFAULT: 'hsl(var(--error))',
          foreground: 'hsl(var(--error-foreground))',
        },
        muted: {
          DEFAULT: 'hsl(var(--muted))',
          foreground: 'hsl(var(--muted-foreground))',
        },
      },
      fontFamily: {
        sans: ['Inter', 'system-ui', 'sans-serif'],
        mono: ['JetBrains Mono', 'monospace'],
      },
      borderRadius: {
        lg: 'var(--radius-lg)',
        md: 'var(--radius-md)',
        sm: 'var(--radius-sm)',
      },
    },
  },
  plugins: [require('tailwindcss-animate')],
}

export default config
```

### shadcn/ui Setup Commands

```bash
cd apps/web

# Initialize shadcn/ui
npx shadcn-ui@latest init

# Add base components
npx shadcn-ui@latest add button
npx shadcn-ui@latest add input
npx shadcn-ui@latest add card
npx shadcn-ui@latest add badge
npx shadcn-ui@latest add avatar
npx shadcn-ui@latest add dropdown-menu
npx shadcn-ui@latest add dialog
npx shadcn-ui@latest add alert
npx shadcn-ui@latest add toast
npx shadcn-ui@latest add label
npx shadcn-ui@latest add separator
```

### Component Directory Structure

```
apps/web/src/components/
├── ui/                     # shadcn/ui components (auto-generated)
│   ├── button.tsx
│   ├── input.tsx
│   ├── card.tsx
│   ├── badge.tsx
│   ├── avatar.tsx
│   ├── dropdown-menu.tsx
│   ├── dialog.tsx
│   ├── alert.tsx
│   └── toast.tsx
├── common/                 # Custom shared components
│   ├── LoadingSpinner.tsx
│   ├── ErrorBoundary.tsx
│   └── index.ts
└── layout/                 # Layout components (Story 1.9)
```

### Custom Components

```tsx
// apps/web/src/components/common/LoadingSpinner.tsx
import { cn } from '@/lib/utils'

interface LoadingSpinnerProps {
  size?: 'sm' | 'md' | 'lg'
  className?: string
  message?: string
}

export function LoadingSpinner({ 
  size = 'md', 
  className,
  message 
}: LoadingSpinnerProps) {
  const sizeClasses = {
    sm: 'h-4 w-4',
    md: 'h-8 w-8',
    lg: 'h-12 w-12',
  }

  return (
    <div className={cn('flex flex-col items-center justify-center gap-2', className)}>
      <div
        className={cn(
          'animate-spin rounded-full border-2 border-muted border-t-primary',
          sizeClasses[size]
        )}
        role="status"
        aria-label="Loading"
      />
      {message && (
        <p className="text-sm text-muted-foreground">{message}</p>
      )}
    </div>
  )
}
```

### Accessibility Requirements (NFR12)

| Requirement | Implementation |
|-------------|----------------|
| Color contrast ≥ 4.5:1 (text) | Verified in design tokens |
| Color contrast ≥ 3:1 (UI) | Verified in design tokens |
| Focus indicators | `ring-2 ring-ring ring-offset-2` |
| Keyboard navigation | Radix primitives handle this |
| ARIA labels | Required on icon-only buttons |

### Focus Styles

```css
/* Global focus styles */
*:focus-visible {
  outline: none;
  ring: 2px;
  ring-color: hsl(var(--ring));
  ring-offset: 2px;
}
```

### Storybook Setup

```bash
cd apps/web
npx storybook@latest init
npm install @storybook/addon-a11y --save-dev
```

```typescript
// apps/web/.storybook/main.ts
import type { StorybookConfig } from '@storybook/react-vite'

const config: StorybookConfig = {
  stories: ['../src/**/*.stories.@(js|jsx|ts|tsx)'],
  addons: [
    '@storybook/addon-links',
    '@storybook/addon-essentials',
    '@storybook/addon-a11y',
  ],
  framework: {
    name: '@storybook/react-vite',
    options: {},
  },
}

export default config
```

### Sample Story

```tsx
// apps/web/src/components/ui/button.stories.tsx
import type { Meta, StoryObj } from '@storybook/react'
import { Button } from './button'

const meta: Meta<typeof Button> = {
  title: 'UI/Button',
  component: Button,
  tags: ['autodocs'],
}

export default meta
type Story = StoryObj<typeof Button>

export const Primary: Story = {
  args: {
    children: 'Button',
  },
}

export const Secondary: Story = {
  args: {
    variant: 'secondary',
    children: 'Secondary',
  },
}

export const Destructive: Story = {
  args: {
    variant: 'destructive',
    children: 'Delete',
  },
}
```

### Package.json Scripts

```json
{
  "scripts": {
    "storybook": "storybook dev -p 6006",
    "build-storybook": "storybook build"
  }
}
```

### Dependencies on Previous Stories

- Story 1.1: Frontend project exists with Tailwind configured

### References

- [Source: architecture.md#Frontend-Architecture] - Component organization
- [Source: architecture.md#UX-Flows-Screen-Architecture] - Design tokens
- [Source: epics.md#Story-1.8] - Original acceptance criteria
- NFR12: WCAG 2.1 AA compliance

## Dev Agent Record

### Agent Model Used

_To be filled by dev agent_

### Completion Notes List

_To be filled during implementation_

### Change Log

_To be filled during implementation_

### File List

_To be filled after implementation_

