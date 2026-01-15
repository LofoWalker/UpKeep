# Design System — Upkeep MVP

**Stack** : React + shadcn/ui + Tailwind CSS  
**Approche** : Minimal, B2B-friendly, trust-first

---

## 1. Design Principles

### Trust-First
- États explicites partout (pas d'ambiguïté)
- Feedback immédiat sur chaque action
- Transparence des règles (guardrails visibles)

### Low Friction
- Minimum de clics pour les actions courantes
- Progressive disclosure (détails à la demande)
- Defaults intelligents (copy from last month, pre-fill from GitHub)

### Enterprise-Ready
- Clean, professional aesthetic
- Accessible (WCAG 2.1 AA)
- Pas de gamification excessive (B2B ≠ B2C)

---

## 2. Color Tokens

### Brand Colors

```css
/* Primary — Action, CTA */
--primary: #2563eb;        /* blue-600 */
--primary-hover: #1d4ed8;  /* blue-700 */
--primary-light: #dbeafe;  /* blue-100 */

/* Secondary — Neutral actions */
--secondary: #64748b;      /* slate-500 */
--secondary-hover: #475569; /* slate-600 */
```

### Semantic Colors

```css
/* Success — Paid, Verified, Completed */
--success: #16a34a;        /* green-600 */
--success-light: #dcfce7;  /* green-100 */

/* Warning — Held, Unclaimed, Approaching limit */
--warning: #d97706;        /* amber-600 */
--warning-light: #fef3c7;  /* amber-100 */

/* Error — Failed, Rejected */
--error: #dc2626;          /* red-600 */
--error-light: #fee2e2;    /* red-100 */

/* Info — Neutral information */
--info: #0891b2;           /* cyan-600 */
--info-light: #cffafe;     /* cyan-100 */
```

### Neutral Colors

```css
/* Backgrounds */
--bg-primary: #ffffff;
--bg-secondary: #f8fafc;   /* slate-50 */
--bg-tertiary: #f1f5f9;    /* slate-100 */

/* Text */
--text-primary: #0f172a;   /* slate-900 */
--text-secondary: #475569; /* slate-600 */
--text-muted: #94a3b8;     /* slate-400 */

/* Borders */
--border-default: #e2e8f0; /* slate-200 */
--border-strong: #cbd5e1;  /* slate-300 */
```

---

## 3. Typography

### Font Stack

```css
--font-sans: 'Inter', system-ui, -apple-system, sans-serif;
--font-mono: 'JetBrains Mono', 'Fira Code', monospace;
```

### Scale

| Token | Size | Weight | Line Height | Usage |
|-------|------|--------|-------------|-------|
| `h1` | 30px | 700 | 1.2 | Page titles |
| `h2` | 24px | 600 | 1.3 | Section headers |
| `h3` | 18px | 600 | 1.4 | Card headers |
| `body` | 14px | 400 | 1.5 | Default text |
| `body-sm` | 13px | 400 | 1.5 | Secondary text |
| `caption` | 12px | 400 | 1.4 | Labels, hints |
| `mono` | 13px | 400 | 1.5 | Package names, code |

---

## 4. Spacing

Base unit: **4px**

```css
--space-1: 4px;
--space-2: 8px;
--space-3: 12px;
--space-4: 16px;
--space-5: 20px;
--space-6: 24px;
--space-8: 32px;
--space-10: 40px;
--space-12: 48px;
--space-16: 64px;
```

---

## 5. Component Library

### Basé sur shadcn/ui

Utiliser les composants shadcn/ui avec customisation minimale :

- `Button`
- `Input`
- `Card`
- `Badge`
- `Progress`
- `Alert`
- `Dialog`
- `DropdownMenu`
- `Tabs`
- `Table`

### Custom Components (à créer)

#### PackageCard

```tsx
interface PackageCardProps {
  name: string;
  description?: string;
  allocation?: number;        // €
  percentage?: number;        // %
  status?: 'paid' | 'held' | 'failed' | 'pending';
  claimStatus?: 'claimed' | 'unclaimed';
  onAdd?: () => void;
  onRemove?: () => void;
  onAdjust?: (delta: number) => void;
}
```

**Variants** :
- `compact` — Liste suggestion (name + downloads + [Add])
- `allocation` — Allocation view (name + € + bar + controls)
- `detail` — Full card avec description

#### BudgetBar

```tsx
interface BudgetBarProps {
  total: number;
  allocated: number;
  currency?: string;
}
```

Affiche : `Budget: €500  Allocated: €340  Remaining: €160` + progress bar

#### GuardrailBadge

```tsx
interface GuardrailBadgeProps {
  rule: 'min-packages' | 'max-percentage';
  satisfied: boolean;
  current?: number;
  target?: number;
}
```

Affiche : `✓ Min 3 packages` ou `⚠ Max 34% per package`

#### PayoutStatusBadge

```tsx
type PayoutStatus = 'paid' | 'held' | 'failed' | 'pending';

interface PayoutStatusBadgeProps {
  status: PayoutStatus;
  label?: string;
}
```

| Status | Color | Icon | Label |
|--------|-------|------|-------|
| `paid` | green | ✓ | Paid |
| `held` | amber | ⏸ | Held |
| `failed` | red | ✗ | Failed |
| `pending` | slate | ◠ | Pending |

#### ProgressStepper

```tsx
interface Step {
  id: string;
  label: string;
  status: 'completed' | 'current' | 'upcoming';
}

interface ProgressStepperProps {
  steps: Step[];
}
```

Pour l'onboarding : `Workspace → Budget → Dependencies → Allocate`

#### MonthNavigator

```tsx
interface MonthNavigatorProps {
  currentMonth: Date;
  onPrevious: () => void;
  onNext: () => void;
  hasNext?: boolean;
}
```

#### StatsCard

```tsx
interface StatsCardProps {
  label: string;
  value: string | number;
  trend?: {
    direction: 'up' | 'down' | 'neutral';
    label: string;
  };
  icon?: React.ReactNode;
}
```

#### EmptyState

```tsx
interface EmptyStateProps {
  icon?: React.ReactNode;
  title: string;
  description: string;
  action?: {
    label: string;
    onClick: () => void;
  };
}
```

#### FileDropzone

```tsx
interface FileDropzoneProps {
  accept: string[];           // ['package-lock.json', 'yarn.lock']
  onFile: (file: File) => void;
  processing?: boolean;
  error?: string;
}
```

---

## 6. Layout Patterns

### Page Layout (Authenticated)

```
┌─────────────────────────────────────────────────────────────┐
│  Navbar (sticky)                                            │
│  - Logo + Workspace name                                    │
│  - User menu (right)                                        │
├─────────────────────────────────────────────────────────────┤
│  Tabs (if applicable)                                       │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  Page Content                                               │
│  - max-width: 1200px                                        │
│  - padding: 24px (desktop), 16px (mobile)                   │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### Card Grid

```
┌─────────────────────┐ ┌─────────────────────┐
│  Stats Card         │ │  Stats Card         │
└─────────────────────┘ └─────────────────────┘
┌─────────────────────┐ ┌─────────────────────┐
│  Stats Card         │ │  Stats Card         │
└─────────────────────┘ └─────────────────────┘
```

Grid: `grid-cols-2` (desktop), `grid-cols-1` (mobile)
Gap: `16px`

### Form Layout

Labels au-dessus des inputs (pas inline).
Vertical stack avec `space-y-4`.

---

## 7. Interaction Patterns

### Loading States

- **Button loading** : Spinner + disabled state
- **Page loading** : Skeleton placeholders
- **Async operation** : Toast notification on complete

### Confirmations

- **Destructive actions** : Dialog avec double confirmation
- **Non-destructive** : Inline confirmation ou toast

### Validation

- **Inline validation** : Messages sous le champ
- **Form-level** : Summary en haut du form (si > 1 erreur)
- **Guardrails** : Prévention plutôt que rejet (disable CTA, explain why)

### Feedback

| Action | Feedback |
|--------|----------|
| Save draft | Toast "Draft saved" |
| Finalize allocation | Success screen |
| File upload | Progress indicator |
| Error | Alert banner + specific message |

---

## 8. Responsive Breakpoints

```css
/* Mobile first */
--breakpoint-sm: 640px;   /* Landscape phones */
--breakpoint-md: 768px;   /* Tablets */
--breakpoint-lg: 1024px;  /* Laptops */
--breakpoint-xl: 1280px;  /* Desktops */
```

**Focus MVP** : Desktop-first (B2B), mobile readable but not optimized for complex interactions.

---

## 9. Accessibility Checklist

### WCAG 2.1 AA Requirements

- [ ] Color contrast ratio ≥ 4.5:1 for text
- [ ] Color contrast ratio ≥ 3:1 for UI components
- [ ] Focus indicators visible
- [ ] Keyboard navigation for all interactive elements
- [ ] Form labels associated with inputs
- [ ] Error messages linked to fields
- [ ] Skip to main content link
- [ ] Alt text for meaningful images
- [ ] ARIA labels for icon-only buttons

### Testing

- axe DevTools
- Keyboard-only navigation test
- Screen reader test (VoiceOver/NVDA)

---

## 10. Icons

**Library** : Lucide Icons (compatible shadcn/ui)

### Semantic Icons

| Concept | Icon | Usage |
|---------|------|-------|
| Package | `Package` | npm packages |
| Budget | `DollarSign` or `Euro` | Money amounts |
| Company | `Building2` | Company entities |
| Maintainer | `User` | Maintainer profiles |
| Success | `CheckCircle` | Paid, verified |
| Warning | `AlertTriangle` | Held, unclaimed |
| Error | `XCircle` | Failed |
| Info | `Info` | Hints, tooltips |
| Add | `Plus` | Add actions |
| Remove | `X` | Remove actions |
| Edit | `Pencil` | Edit actions |
| Settings | `Settings` | Settings |
| Export | `Download` | CSV export |
| External | `ExternalLink` | External links |

---

## 11. Motion

### Principles

- **Subtle** : Pas de distractions
- **Purposeful** : Feedback, pas décoration
- **Fast** : 150-200ms pour les micro-interactions

### Transitions

```css
--transition-fast: 150ms ease-out;
--transition-normal: 200ms ease-out;
--transition-slow: 300ms ease-out;
```

### Usage

| Element | Transition |
|---------|------------|
| Button hover | `fast` |
| Dropdown open | `normal` |
| Modal open | `slow` |
| Toast enter/exit | `normal` |
| Progress bar | `slow` |

---

## 12. Dark Mode

**MVP** : Light mode only.

**Post-MVP** : Support dark mode via CSS variables et Tailwind `dark:` classes.

---

## 13. Implementation Notes

### shadcn/ui Setup

```bash
npx shadcn-ui@latest init
npx shadcn-ui@latest add button input card badge progress alert dialog dropdown-menu tabs table
```

### Tailwind Config Extensions

```js
// tailwind.config.js
module.exports = {
  theme: {
    extend: {
      colors: {
        // Custom semantic colors if needed
      },
      fontFamily: {
        sans: ['Inter', 'system-ui', 'sans-serif'],
        mono: ['JetBrains Mono', 'monospace'],
      },
    },
  },
};
```

### Component Organization

```
src/
├── components/
│   ├── ui/              # shadcn/ui components
│   │   ├── button.tsx
│   │   ├── input.tsx
│   │   └── ...
│   ├── common/          # Custom shared components
│   │   ├── PackageCard.tsx
│   │   ├── BudgetBar.tsx
│   │   ├── GuardrailBadge.tsx
│   │   ├── PayoutStatusBadge.tsx
│   │   ├── ProgressStepper.tsx
│   │   ├── MonthNavigator.tsx
│   │   ├── StatsCard.tsx
│   │   ├── EmptyState.tsx
│   │   └── FileDropzone.tsx
│   ├── layout/          # Layout components
│   │   ├── Navbar.tsx
│   │   ├── PageLayout.tsx
│   │   └── TabNav.tsx
│   └── features/        # Feature-specific components
│       ├── allocation/
│       ├── onboarding/
│       └── ...
```

---

## Checklist avant dev

- [ ] shadcn/ui initialisé
- [ ] Inter font ajoutée
- [ ] Tailwind config étendu
- [ ] Composants custom créés (stubs)
- [ ] Color tokens validés (contrast check)
- [ ] Responsive breakpoints testés

