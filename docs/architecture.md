---
stepsCompleted: [step-01-init, step-02-context, step-03-starter, step-04-decisions, step-05-patterns, step-06-ux-integration]
inputDocuments:
  - _bmad-output/planning-artifacts/prd.md
  - _bmad-output/planning-artifacts/ux/README.md
  - _bmad-output/planning-artifacts/ux/design-system.md
  - _bmad-output/planning-artifacts/ux/wireframes-company-onboarding.md
  - _bmad-output/planning-artifacts/ux/wireframes-maintainer-flow.md
  - _bmad-output/planning-artifacts/ux/wireframes-company-dashboard.md
  - _bmad-output/planning-artifacts/ux/wireframes-admin-support.md
  - docs/global-idea.md
workflowType: 'architecture'
project_name: 'Upkeep'
user_name: 'Lofo'
date: '2026-01-09'
---

# Architecture Decision Document

_This document builds collaboratively through step-by-step discovery. Sections are appended as we work through each architectural decision together._

## Project Context Analysis

### Requirements Overview

**Functional Requirements (capability inventory):**
The PRD defines **41 functional requirements** across these main capability areas:

- Identity, workspaces & access control
- Company budgeting & monthly allocations (with guardrails)
- Sponsorship transparency (company page public opt-in)
- Maintainer onboarding + package claiming + eligibility
- Payouts / money movement (explicit paid/held/failed states)
- Operations & support tooling (investigation, retries)
- Auditability & reporting (CSV exports)

Architecturally, this implies a multi-entity system with strict tenant boundaries and a money-moving domain that must be modeled as explicit state machines (allocation snapshots, payout runs, claim verification states).

**Non-Functional Requirements (quality drivers):**
The PRD’s key NFRs that will drive architecture are:

- **Security / isolation:** strong tenant isolation, encryption at rest, TLS everywhere, audit trail for any money-impacting action.
- **Reliability:** payout runs must be idempotent; no “unknown money state” (every allocation-period ends in an explicit outcome with an explanation).
- **Performance:** core pages p95 ≤ 2s under MVP load; imports give feedback and complete ≤ 2 minutes for typical lockfiles.
- **Accessibility:** WCAG 2.1 AA for core flows.
- **Observability:** payout runs and failures must be traceable via logs/metrics correlated by identifiers.

**Scale & Complexity:**
- Primary domain: **full-stack web SaaS** (B2B)
- Complexity level: **medium** (money movement + claims + auditability)
- Estimated architectural components: ~**8–12** (auth, tenancy, packages, allocations, payouts, claims/verification, public pages, admin/support, audit, exports)

### Technical Constraints & Dependencies

Known constraints & dependencies from PRD:

- **Ecosystem scope V1:** npm packages.
- **No deep integrations required in MVP:** dependency import via lockfile upload / paste list.
- **Payments/payouts delegated to a provider:** platform should not store raw payment instrument details.
- **Public transparency is opt-in:** default private; public page must not leak internal company data.

### Cross-Cutting Concerns Identified

- **Multi-tenancy & authorization**: every read/write is company-scoped; role-based restrictions on money-impacting actions.
- **Audit trail**: immutable event stream for budget/allocation/payout/claim transitions.
- **State machines**: allocation lifecycle, claim verification lifecycle, payout run lifecycle.
- **Failure handling**: explicit error surfaces for payout failures/unclaimed funds, and retry semantics.
- **Abuse prevention guardrails**: minimum dispersion + per-package cap at allocation time.
- **Privacy boundaries**: public company pages show only safe aggregates.

This matches a product where correctness and traceability matter more than “feature breadth”.

## Starter Template Evaluation

### Primary Technology Domain

Full-stack SaaS with:
- Web frontend (B2B dashboards + public company pages)
- Backend API + background jobs (payout runs)
- Relational data model + audit/event trail

### Starter Options Considered

**Option A: React + shadcn/ui + TailwindCSS + Quarkus + Postgres (recommended)**
- React ecosystem for UI (matches UX team's design system based on shadcn/ui).
- shadcn/ui provides accessible, customizable components.
- TailwindCSS for styling with design tokens.
- Quarkus for the API/business domain, Postgres for data.
- Allows strict separation of concerns: frontend (SPA) and backend (API) can evolve independently.

**Option B: Angular + TailwindCSS (rejected)**
- Doesn't match the UX artifacts which specify React + shadcn/ui.

### Selected Starter: React (Vite) + Quarkus

**Rationale for Selection:**
- Matches the UX design system: React, shadcn/ui, TailwindCSS.
- Quarkus + Postgres for robust backend.
- Stripe for payments.
- Keeps MVP buildable without premature complexity (no microservices, no distributed system).

**Initialization Commands (versions pinned at init time to latest LTS):**

```bash
# frontend (React + Vite + TypeScript)
npm create vite@latest upkeep-web -- --template react-ts
cd upkeep-web
npm install
npx shadcn-ui@latest init
npx shadcn-ui@latest add button input card badge progress alert dialog dropdown-menu tabs table

# backend
mvn io.quarkus.platform:quarkus-maven-plugin:create \
  -DprojectGroupId=com.upkeep \
  -DprojectArtifactId=upkeep-api \
  -DclassName="com.upkeep.api.HealthResource" \
  -Dpath="/health"
```

## Core Architectural Decisions

### Decision Priority Analysis

**Critical Decisions (Block Implementation):**
- System boundaries: SPA frontend + API backend + background jobs
- Tenant isolation model
- Authentication + authorization model
- Money movement state machines (allocations, claims, payout runs)
- Data model strategy + migrations
- Stripe integration boundaries (no storing payment instruments)

**Important Decisions (Shape Architecture):**
- API style (REST)
- Event/audit log as first-class capability
- Import pipeline for lockfiles
- Observability and failure handling conventions

**Deferred Decisions (Post-MVP):**
- GitHub App auto-sync
- SSO/SAML
- Multi-currency/invoicing
- Advanced RBAC / approvals
- Multi-ecosystem packages

### Data Architecture

**Database:** PostgreSQL (latest LTS at provisioning time)

**Core modeling approach:**
- Relational, normalized core entities for correctness and reporting.
- Time-based immutability where it matters (allocation snapshots; payout run records).

**Migrations:**
- Managed migrations owned by the backend service (repeatable in CI).

**Caching:**
- No distributed cache in MVP.
- Use application-level caching only where safe (e.g., public company page aggregates) with short TTL and clear invalidation.

**Key data entities (MVP-level):**
- Company, User, Membership/Role
- Package (npm), PackageAlias (if needed), PackageClaim, ClaimVerification
- Budget, AllocationDraft, AllocationSnapshot
- PayoutRun, PayoutLineItem (per package), PayoutOutcome
- AuditEvent

### Authentication & Security

**Authentication:**
- Centralized identity in the backend.
- Frontend uses token-based auth against the API.

**Authorization (RBAC):**
- Roles: Owner / Member (as per PRD).
- Money-impacting actions (budget change, finalize allocation, payout runs, claim verification changes) require Owner or internal-ops.

**Tenant isolation:**
- All reads/writes are scoped by `company_id`.
- Public company pages use a separate “public view” query path that only exposes approved aggregates.

**Encryption:**
- TLS everywhere.
- Encryption at rest enabled at the storage layer.

### API & Communication Patterns

**API style:** REST JSON.

**API boundaries (high level):**
- Auth & identity
- Company/workspace management
- Packages & imports
- Budgeting & allocations
- Maintainer onboarding + claims
- Payouts + payout runs
- Public company pages
- Admin/support investigation

**Error handling:**
- Standard error envelope with stable error codes.
- Explicit domain errors for payout outcomes and claim verification.

**Rate limiting:**
- Minimal in MVP; enforce on public endpoints if abuse appears.

### Frontend Architecture

**Frontend:** React SPA (Vite + TypeScript).

**UI Component Library:** shadcn/ui (over Radix primitives).

**Styling:** TailwindCSS with design tokens.

**State management:**
- Keep MVP simple: React Query (TanStack Query) for server state.
- Local state with useState/useReducer where appropriate.
- Introduce Zustand or similar only if client-side complexity grows.

**Design System Integration (from UX artifacts):**

| Token Type | Implementation |
|------------|----------------|
| Colors | CSS variables (`--primary`, `--success`, `--warning`, `--error`) mapped to Tailwind config |
| Typography | Inter (sans), JetBrains Mono (mono) via `font-sans`/`font-mono` |
| Spacing | 4px base unit (Tailwind `space-*`) |
| Breakpoints | Mobile-first: sm (640px), md (768px), lg (1024px), xl (1280px) |

**Custom Components (from design-system.md):**

| Component | Purpose |
|-----------|---------|
| `PackageCard` | Display package with allocation, status, and claim info |
| `BudgetBar` | Show total/allocated/remaining with progress bar |
| `GuardrailBadge` | Display rule satisfaction (min packages, max %) |
| `PayoutStatusBadge` | Status indicator: paid/held/failed/pending |
| `ProgressStepper` | Onboarding progress (Workspace → Budget → Dependencies → Allocate) |
| `MonthNavigator` | Navigate between allocation periods |
| `StatsCard` | KPI display with optional trend indicator |
| `EmptyState` | Placeholder for empty lists with action CTA |
| `FileDropzone` | Lockfile upload with drag-and-drop |

**Component Organization:**

```
src/
├── components/
│   ├── ui/              # shadcn/ui components
│   ├── common/          # Custom shared components (PackageCard, BudgetBar, etc.)
│   ├── layout/          # Navbar, PageLayout, TabNav
│   └── features/        # Feature-specific (allocation/, onboarding/, maintainer/, admin/)
├── hooks/               # Custom React hooks
├── lib/                 # Utilities (api client, formatters)
├── pages/               # Route-level components
└── styles/              # Global styles, design tokens
```

**Routing:**
- Separate routes for:
  - Public pages (`/[company-slug]` - sponsorship page)
  - Authenticated app (`/app/*` - company dashboard)
  - Maintainer portal (`/maintainer/*`)
  - Admin panel (`/admin/*` - internal ops)

### Infrastructure & Deployment

**Deployment unit:**
- Two deployables (web + API), plus database.

**Background jobs:**
- Implement payout runs as backend scheduled jobs.
- Ensure idempotency and strong observability.

**Observability:**
- Structured logs with correlation IDs: company_id, package_id, payout_run_id.
- Metrics around payout runs success/fail/held.

## Implementation Patterns & Consistency Rules

### Pattern Categories Defined

**Critical conflict points identified:** naming, request/response formats, auth propagation, tenant scoping, money state machines, audit trail, error semantics.

### Naming Patterns

**Database naming conventions (PostgreSQL):**
- Tables: `snake_case` plural (e.g., `companies`, `users`, `company_memberships`, `packages`, `allocation_snapshots`, `payout_runs`, `audit_events`).
- Columns: `snake_case` (e.g., `company_id`, `created_at`).
- Primary keys: `id` (UUID).
- Foreign keys: `<ref>_id` (e.g., `company_id`, `package_id`).
- Timestamps: `created_at`, `updated_at`, optional `deleted_at`.
- Indexes: `idx_<table>__<cols>` (e.g., `idx_packages__ecosystem_name`).
- Unique constraints: `uq_<table>__<cols>`.

**API naming conventions (REST):**
- Base path: `/api`.
- Resources are plural nouns: `/companies`, `/packages`, `/allocations`, `/payout-runs`.
- Identifiers in path: `/companies/{companyId}`.
- Relationship paths: `/companies/{companyId}/members`, `/companies/{companyId}/allocations`.
- Public endpoints are explicitly namespaced: `/public/companies/{slug}`.
- Query params: `camelCase` (Angular default ergonomics), but never leak db naming (e.g., `page`, `pageSize`, `sortBy`).

**Code naming conventions:**
- React (TypeScript):
  - Components: `PascalCase` (e.g., `PackageCard.tsx`).
  - Hooks: `useCamelCase` (e.g., `useAllocation.ts`).
  - Utilities: `camelCase` (e.g., `formatCurrency.ts`).
  - Types/interfaces: `PascalCase` (e.g., `Package`, `AllocationDraft`).
- Quarkus (Hexagonal Architecture):
  - Packages: `com.upkeep.{layer}.{sublayer}` (e.g., `com.upkeep.application.port.in`).
  - Use cases (ports in): `XxxUseCase` interface, `XxxUseCaseImpl` implementation.
  - Repositories (ports out): `XxxRepository` interface.
  - REST resources: `XxxResource` (in `adapter/in/rest/<domain>/`).
  - REST DTOs: `XxxRequest`, `XxxResponse` (co-located with resource).
  - JPA entities: `XxxEntity` (in `adapter/out/persistence/`).
  - Mappers: `XxxMapper` (MapStruct interfaces).
  - Domain models: `Xxx` (pure domain classes in `domain/model/<aggregate>/`).

### Structure Patterns

**Repository layout (monorepo recommended):**
- `apps/web/` (React + Vite)
- `apps/api/` (Quarkus)
- `libs/` (shared contracts if needed later)

**Backend architecture (Hexagonal / Ports & Adapters):**

```
com.upkeep/
├── application/
│   ├── port/
│   │   ├── in/                    # Use case interfaces (driving ports)
│   │   │   └── XxxUseCase.java
│   │   └── out/                   # Repository/service interfaces (driven ports)
│   │       └── XxxRepository.java
│   └── usecase/                   # Use case implementations
│       └── XxxUseCaseImpl.java
├── domain/
│   ├── model/                     # Domain entities & value objects
│   │   └── <aggregate>/
│   │       ├── Entity.java
│   │       └── ValueObject.java
│   └── exception/                 # Domain exceptions (business-oriented)
│       ├── DomainException.java   # Abstract base class
│       ├── DomainValidationException.java
│       ├── InvalidCredentialsException.java
│       ├── CustomerNotFoundException.java
│       ├── CustomerAlreadyExistsException.java
│       └── FieldError.java
└── infrastructure/
    └── adapter/
        ├── in/                    # Driving adapters (HTTP, CLI, etc.)
        │   └── rest/
        │       ├── <domain>/      # Per-domain REST resources & DTOs
        │       │   ├── XxxResource.java
        │       │   ├── XxxRequest.java
        │       │   └── XxxResponse.java
        │       └── common/        # Shared REST components
        │           ├── exception/
        │           │   └── GlobalExceptionMapper.java  # Maps domain exceptions to HTTP
        │           └── response/
        │               ├── ApiResponse.java
        │               ├── ApiError.java
        │               └── ApiMeta.java
        └── out/                   # Driven adapters (DB, email, external APIs)
            ├── persistence/       # JPA entities & repositories
            │   ├── XxxEntity.java
            │   ├── XxxJpaRepository.java
            │   └── XxxMapper.java
            ├── email/             # Email service implementations
            │   └── XxxEmailService.java
            └── security/          # Security adapters (password hashing, JWT)
                └── XxxService.java
```

**Layer responsibilities:**
- **Domain**: Pure business logic, no framework dependencies. Exceptions are business-oriented.
- **Application**: Orchestrates use cases, defines port interfaces.
- **Infrastructure**: Implements adapters for external systems (DB, HTTP, email). Maps domain exceptions to HTTP responses.

**Domain exceptions pattern (DDD-compliant):**

Domain exceptions must follow Domain-Driven Design principles:

1. **Business-oriented naming**: Exceptions describe business rule violations, not technical/HTTP concepts.
   - ✅ `InvalidCredentialsException`, `CustomerNotFoundException`, `AllocationGuardrailViolationException`
   - ❌ `UnauthorizedException`, `NotFoundException`, `BadRequestException`

2. **Domain language (Ubiquitous Language)**: Exception names use domain terminology understood by business stakeholders.
   - ✅ `PackageClaimAlreadyExistsException` (domain concept)
   - ❌ `ConflictException` (technical HTTP concept)

3. **Single responsibility**: Each exception represents one specific business rule violation.
   - ✅ `BudgetExceededException`, `MinimumPackagesNotMetException`
   - ❌ `ValidationException` (too generic)

4. **Context-rich**: Exceptions carry domain context needed for error handling.
   - Include relevant identifiers (e.g., `customerId`, `packageName`)
   - Provide a human-readable message in domain terms

5. **Framework-agnostic**: Domain exceptions have no dependencies on HTTP, JAX-RS, or any framework.
   - All domain exceptions extend `DomainException` (abstract base class)
   - HTTP mapping happens in infrastructure layer (`GlobalExceptionMapper`)

**Exception hierarchy:**
```
DomainException (abstract)
├── DomainValidationException      # Field-level validation errors
├── InvalidCredentialsException    # Authentication failure
├── InvalidRefreshTokenException   # Token expired/revoked/not found
├── CustomerNotFoundException      # Customer lookup failed
├── CustomerAlreadyExistsException # Duplicate email
├── PackageClaimAlreadyExistsException
├── AllocationGuardrailViolationException
└── ... (one exception per distinct business rule violation)
```

**Infrastructure mapping (`GlobalExceptionMapper`):**
| Domain Exception | HTTP Status | Error Code |
|------------------|-------------|------------|
| `DomainValidationException` | 400 | `VALIDATION_ERROR` |
| `InvalidCredentialsException` | 401 | `INVALID_CREDENTIALS` |
| `InvalidRefreshTokenException` | 401 | `INVALID_TOKEN` |
| `CustomerNotFoundException` | 404 | `CUSTOMER_NOT_FOUND` |
| `CustomerAlreadyExistsException` | 409 | `CUSTOMER_ALREADY_EXISTS` |
| `AllocationGuardrailViolationException` | 422 | `GUARDRAIL_VIOLATION` |
| Other `DomainException` | 422 | `DOMAIN_ERROR` |

**Entity mapping**: Use MapStruct for converting between domain objects and JPA entities.
- Mappers are defined as interfaces annotated with `@Mapper(componentModel = "cdi")`.
- MapStruct generates implementations at compile time and registers them as CDI beans.
- Complex mappings (e.g., value objects to primitives) use custom mapping methods in the interface.

**Frontend organization (React):**
- Feature-first (reduces cross-agent conflicts):
  - `features/auth/*`
  - `features/company/*`
  - `features/allocation/*`
  - `features/maintainer/*`
  - `features/public-company/*`
  - `features/admin/*`
  - `shared/*` (ui, utils)

### Format Patterns

**API response envelope (consistent, testable):**
- Success: `{ "data": <payload>, "meta": { ... } }`
- Error: `{ "error": { "code": "STRING_CODE", "message": "Human readable", "details": { ... }, "traceId": "..." } }`

**HTTP status conventions:**
- `200/201` for success.
- `400` validation errors.
- `401` unauthenticated.
- `403` unauthorized.
- `404` not found.
- `409` conflict (e.g., claim already exists).
- `422` domain rule violated (e.g., allocation guardrails).
- `500` unexpected.

**Date/time:**
- ISO-8601 UTC strings (`YYYY-MM-DDTHH:mm:ss.sssZ`).

**Identifiers:**
- UUIDs as strings.

**JSON field naming:**
- `camelCase` for all API payloads (frontend ergonomic); mapping happens at backend boundary.

### Communication & State Machine Patterns

**State machines are explicit and persisted:**
- AllocationSnapshot status: `DRAFT` → `FINALIZED`.
- Claim status: `PENDING` → `VERIFIED` | `REJECTED`.
- PayoutRun status: `CREATED` → `RUNNING` → `COMPLETED` | `FAILED`.
- Per-package payout outcome: `PAID` | `HELD_UNCLAIMED` | `FAILED`.

**Idempotency rules:**
- Payout runs accept an idempotency key per run.
- External provider calls (Stripe) must use idempotency keys.

### Process Patterns

**Tenant scoping is mandatory:**
- Every authenticated request resolves `companyId` from path + membership.
- Never accept `companyId` solely from body.

**Auth propagation:**
- Frontend stores access token securely and sends it via `Authorization: Bearer <token>`.
- Backend adds a `traceId` to every response.

**Validation patterns:**
- Validate at boundary:
  - frontend form validation for UX
  - backend validation as source of truth

**Audit trail rules:**
- Any money-impacting action must emit an `AuditEvent` with:
  - actor (user id or internal system), company id, action code, target type/id, timestamp, diff/metadata.

**Job scheduling:**
- Payout runs are executed as scheduled jobs in the backend.
- Job execution logs include `payout_run_id` and have a deterministic run summary.

**Observability conventions:**
- Structured logs include: `traceId`, `companyId`, `packageId` (when relevant), `payoutRunId`.
- Metrics counters: payouts paid/held/failed per run.

## UX Flows & Screen Architecture

> Cette section détaille les flux UX critiques identifiés dans les wireframes et leur mapping sur l'architecture.

### Critical User Journeys

**Journey 1: Company Onboarding → First Allocation (TTFA < 10 min)**

```
Landing → Signup → Create Workspace → Set Budget → Import Deps → Allocate → Confirm → Success + Share
```

| Step | Screen | Route | API Calls |
|------|--------|-------|-----------|
| 1 | Landing Page | `/` | — |
| 2 | Signup | `/signup` | `POST /auth/signup` (OAuth or email) |
| 3 | Create Workspace | `/onboarding/workspace` | `POST /companies` |
| 4 | Set Budget | `/onboarding/budget` | `PATCH /companies/{id}/budget` |
| 5 | Import Dependencies | `/onboarding/import` | `POST /companies/{id}/import` |
| 6 | Allocate | `/onboarding/allocate` | `POST /companies/{id}/allocations/drafts` |
| 7 | Confirm | `/onboarding/confirm` | `POST /companies/{id}/allocations/drafts/{id}/finalize` |
| 8 | Success | `/onboarding/success` | — |

**Key Components Used:** `ProgressStepper`, `FileDropzone`, `PackageCard`, `BudgetBar`, `GuardrailBadge`

---

**Journey 2: Maintainer Claim (< 15 min)**

```
Discovery → Signup → Profile → Claim Package → Verify → Connect Payout → Dashboard
```

| Step | Screen | Route | API Calls |
|------|--------|-------|-----------|
| 1 | Discovery | `/[company-slug]` or `/for-maintainers` | — |
| 2 | Signup | `/signup?type=maintainer` | `POST /auth/signup` |
| 3 | Profile | `/maintainer/profile` | `PATCH /maintainers/{id}` |
| 4 | Claim | `/maintainer/claim` | `POST /packages/{id}/claims` |
| 5 | Verify | `/maintainer/verify/{claimId}` | `POST /claims/{id}/verify` |
| 6 | Payout | `/maintainer/payout` | `POST /maintainers/{id}/payout-methods` |
| 7 | Dashboard | `/maintainer/dashboard` | `GET /maintainers/{id}/dashboard` |

**Verification Methods:**
- npm publish access (recommended)
- GitHub repository access
- Manual verification (fallback)

---

**Journey 3: Monthly Allocation Management**

```
Dashboard → Edit Allocation → Adjust Packages → Save Draft / Finalize
```

| Screen | Route | API Calls |
|--------|-------|-----------|
| Dashboard | `/app/dashboard` | `GET /companies/{id}/dashboard` |
| Edit Allocation | `/app/allocations/edit/{period}` | `GET /companies/{id}/allocations/drafts/{period}` |
| — | — | `PATCH /companies/{id}/allocations/drafts/{period}` |
| — | — | `POST /companies/{id}/allocations/drafts/{period}/finalize` |

**Key Interactions:**
- Copy from previous month (default)
- Adjust with `[−][+]` controls
- Add from remaining dependencies via search
- Guardrails validated in real-time (min 3 packages, max 34% per package)

---

**Journey 4: Admin Payout Run Investigation**

```
Payout Runs Dashboard → Run Detail → Individual Payout → Manual Actions
```

| Screen | Route | API Calls |
|--------|-------|-----------|
| Runs Dashboard | `/admin/payout-runs` | `GET /admin/payout-runs` |
| Run Detail | `/admin/payout-runs/{runId}` | `GET /admin/payout-runs/{runId}` |
| Payout Detail | `/admin/payouts/{payoutId}` | `GET /admin/payouts/{payoutId}` |
| Manual Actions | — | `POST /admin/payouts/{id}/retry`, `POST /admin/payouts/{id}/contact` |

**Admin Screens:**
- Companies List & Detail (with activity timeline, impersonation)
- Maintainers List & Detail (with claims, KYC status)
- Packages List & Detail (with funding breakdown)
- Support Queue

---

### Screen Inventory

#### Public Screens (No Auth)
| Screen | Route | Description |
|--------|-------|-------------|
| Landing | `/` | Value prop + CTA "Get Started" |
| Company Public Page | `/[company-slug]` | Opt-in sponsorship transparency |
| For Maintainers | `/for-maintainers` | Maintainer landing page |
| Sign In | `/signin` | OAuth (GitHub, Google) + email |
| Sign Up | `/signup` | Account creation |

#### Company Portal (Auth Required)
| Screen | Route | Description |
|--------|-------|-------------|
| Onboarding (4 steps) | `/onboarding/*` | Wizard: workspace → budget → import → allocate |
| Dashboard | `/app/dashboard` | Monthly overview + KPIs + alerts |
| Edit Allocation | `/app/allocations/edit/{period}` | Modify next month's allocation |
| Packages | `/app/packages` | All imported packages with claim status |
| Package Detail | `/app/packages/{id}` | Funding history per package |
| Payouts | `/app/payouts` | Payout history with filters + export |
| Team | `/app/team` | Members, invites, roles (Owner/Member) |
| Settings | `/app/settings` | Company settings + sponsorship page config |

#### Maintainer Portal (Auth Required)
| Screen | Route | Description |
|--------|-------|-------------|
| Dashboard | `/maintainer/dashboard` | Packages claimed + earnings summary |
| Claim | `/maintainer/claim` | Search & claim packages |
| Verify | `/maintainer/verify/{claimId}` | Ownership verification flow |
| Payout Setup | `/maintainer/payout` | Connect Stripe/PayPal |
| Profile | `/maintainer/profile` | Public profile settings |

#### Admin Panel (Internal Auth)
| Screen | Route | Description |
|--------|-------|-------------|
| Dashboard | `/admin` | Ops KPIs + alerts |
| Payout Runs | `/admin/payout-runs` | Run history + manual actions |
| Run Detail | `/admin/payout-runs/{id}` | Per-run breakdown |
| Payout Detail | `/admin/payouts/{id}` | Full timeline + funding sources |
| Companies | `/admin/companies` | Company list + drill-down |
| Maintainers | `/admin/maintainers` | Maintainer list + claims |
| Packages | `/admin/packages` | Package registry |
| Support Queue | `/admin/support` | Tickets + investigation tools |

---

### Key UI Patterns (from Design System)

**Trust-First Patterns:**
- Explicit states everywhere (no ambiguity)
- Immediate feedback on every action
- Visible guardrails (`GuardrailBadge`)

**Low Friction Patterns:**
- Progressive disclosure (details on demand)
- Smart defaults (copy from last month, pre-fill from GitHub)
- Minimum clicks for common actions

**Interaction Feedback:**
| Action | Feedback |
|--------|----------|
| Save draft | Toast "Draft saved" |
| Finalize allocation | Success screen |
| File upload | Progress indicator |
| Error | Alert banner + specific message |

**Loading States:**
- Button: spinner + disabled
- Page: skeleton placeholders
- Async operation: toast on complete

---

### Accessibility Requirements

**WCAG 2.1 AA Compliance:**
- Color contrast ratio ≥ 4.5:1 for text
- Color contrast ratio ≥ 3:1 for UI components
- Focus indicators visible
- Keyboard navigation for all interactive elements
- Form labels associated with inputs
- ARIA labels for icon-only buttons

**Testing Tools:**
- axe DevTools
- Keyboard-only navigation test
- Screen reader test (VoiceOver/NVDA)

