---
stepsCompleted: [step-01-validate-prerequisites, step-02-design-epics, step-03-create-stories, step-04-final-validation]
workflowStatus: COMPLETED
inputDocuments:
  - _bmad-output/planning-artifacts/prd.md
  - _bmad-output/planning-artifacts/architecture.md
  - _bmad-output/planning-artifacts/ux/README.md
  - _bmad-output/planning-artifacts/ux/design-system.md
  - _bmad-output/planning-artifacts/ux/wireframes-company-onboarding.md
  - _bmad-output/planning-artifacts/ux/wireframes-maintainer-flow.md
  - _bmad-output/planning-artifacts/ux/wireframes-company-dashboard.md
  - _bmad-output/planning-artifacts/ux/wireframes-admin-support.md
---

# Upkeep - Epic Breakdown

## Overview

This document provides the complete epic and story breakdown for Upkeep, decomposing the requirements from the PRD, UX Design, and Architecture into implementable stories.

## Requirements Inventory

### Functional Requirements

**Identity, Workspaces & Access Control**
- FR1: A visitor can create an account and sign in to Upkeep.
- FR2: A signed-in user can create a company workspace.
- FR3: A company Owner can invite a user to join their company workspace.
- FR4: An invited user can accept an invitation and join a company workspace.
- FR5: A company Owner can assign a role to a company member (Owner or Member).
- FR6: A signed-in user can switch between company workspaces they belong to (if applicable).
- FR7: The system restricts all company data access to members of that company workspace.

**Company Budgeting & Allocation**
- FR8: A company Owner can set a monthly open-source budget for their company.
- FR9: A company Owner can update the monthly budget.
- FR10: A company Member can import a list of npm packages into the company workspace.
- FR11: A company Member can view the imported package list.
- FR12: A company Member can create a monthly allocation draft across packages.
- FR13: The system enforces allocation guardrails (minimum number of packages; maximum share per package).
- FR14: A company Owner can finalize a monthly allocation.
- FR15: A company member can view monthly allocation history.
- FR16: A company member can export allocations for a given period.

**Sponsorship Transparency (Company Page)**
- FR17: A company Owner can enable or disable a public sponsorship page for their company.
- FR18: A visitor can view a public company sponsorship page when enabled.
- FR19: A visitor can see the list of funded packages and an aggregate monthly amount on a public company sponsorship page.
- FR20: A company member can view a private sponsorship view even when public sharing is disabled.

**Maintainer Onboarding, Package Claiming & Eligibility**
- FR21: A visitor can create a maintainer account.
- FR22: A maintainer can create a maintainer profile.
- FR23: A maintainer can initiate a package claim for an npm package.
- FR24: The system can record and track the claim verification status for a package.
- FR25: A maintainer can view which packages they have claimed and their claim statuses.
- FR26: The system can represent whether a package has any eligible maintainer(s) for payouts.

**Payouts & Money Movement**
- FR27: The system can calculate payout distributions per package based on finalized company allocations.
- FR28: The system can execute a payout run on a defined cadence.
- FR29: The system can represent payout outcomes per package as explicit states (e.g., paid, held/unclaimed, failed).
- FR30: A company member can view payout outcomes for allocations they made.
- FR31: A maintainer can connect a payout method to receive funds.
- FR32: A maintainer can view payout history and expected payouts by package.

**Operations, Support & Dispute Handling**
- FR33: An internal operator can view payout run summaries (totals, paid, held, failed).
- FR34: An internal operator can retry failed payouts.
- FR35: An internal support user can search by company and/or package to investigate allocation and payout history.
- FR36: The system can provide a clear explanation for held/unclaimed package funds.

**Auditability & Traceability**
- FR37: The system records an audit event when a company budget is created or updated.
- FR38: The system records an audit event when an allocation is finalized.
- FR39: The system records an audit event for payout run execution and outcomes.
- FR40: The system records an audit event for package claim and claim verification changes.

**Reporting & Exports**
- FR41: A company member can export a CSV report of allocations and payouts for a given time range.

### NonFunctional Requirements

**Performance**
- NFR1: Core interactive pages (sign-in, workspace selection, allocation editing, payout outcomes) return primary content within 2 seconds for p95 under expected MVP load.
- NFR2: Import processing provides user-visible progress and completes within 2 minutes for a typical lockfile (or clearly reports failure with actionable guidance).

**Security**
- NFR3: All network traffic uses TLS (HTTPS) for all user-facing and API endpoints.
- NFR4: Sensitive data is encrypted at rest.
- NFR5: Strong tenant isolation: company-scoped data is only accessible to members of that company workspace.
- NFR6: All money-impacting actions (budget changes, allocation finalization, payout run execution, claim verification changes) are authenticated, authorized, and recorded in an immutable audit trail.
- NFR7: Authentication supports secure password storage and account recovery flows.
- NFR8: The platform does not store raw payment instrument details; payments/payouts are delegated to a specialized provider.

**Reliability**
- NFR9: Payout runs are repeatable and idempotent: re-running a payout run does not double-pay.
- NFR10: The system is never in an "unknown money state": each package allocation for each period ends in an explicit outcome (paid / held-unclaimed / failed) with an explanation.

**Scalability**
- NFR11: Architecture supports at least 10x growth in number of companies and packages without fundamental redesign (horizontal scaling acceptable).

**Accessibility**
- NFR12: Public and authenticated web UI meets WCAG 2.1 AA for core flows in MVP (sign-in, allocation, public company page).

**Observability (Ops)**
- NFR13: Operational users can observe payout runs via structured logs/metrics and correlate events by company/package/run identifiers.
- NFR14: Error reporting and alerting exist for payout failures and claim verification failures.

### Additional Requirements

**From Architecture - Starter Template & Tech Stack**
- Monorepo structure: `apps/web/` (React + Vite), `apps/api/` (Quarkus), `infra/`
- React + shadcn/ui + TailwindCSS frontend
- Quarkus + PostgreSQL backend
- Stripe Connect for payments/payouts

**From Architecture - State Machines**
- AllocationSnapshot status: DRAFT → FINALIZED
- Claim status: PENDING → VERIFIED | REJECTED
- PayoutRun status: CREATED → RUNNING → COMPLETED | FAILED
- Per-package payout outcome: PAID | HELD_UNCLAIMED | FAILED

**From Architecture - API Patterns**
- REST JSON API with standard envelope: `{ data, meta }` / `{ error }`
- HTTP status conventions: 200/201 success, 400 validation, 401/403 auth, 404 not found, 409 conflict, 422 domain rule, 500 unexpected
- UUIDs as identifiers, ISO-8601 UTC dates, camelCase JSON fields

**From Architecture - Data Model**
- Core entities: Company, User, Membership/Role, Package, PackageClaim, ClaimVerification, Budget, AllocationDraft, AllocationSnapshot, PayoutRun, PayoutLineItem, PayoutOutcome, AuditEvent
- All reads/writes scoped by company_id (tenant isolation)

**From UX - Custom Components (P0)**
- `ProgressStepper`: Onboarding multi-step
- `PackageCard`: Display package with allocation, status, claim info
- `BudgetBar`: Show total/allocated/remaining with progress bar
- `GuardrailBadge`: Display rule satisfaction (min 3 packages, max 34%)
- `PayoutStatusBadge`: Status indicator paid/held/failed/pending
- `EmptyState`: Placeholder for empty lists with action CTA
- `FileDropzone`: Lockfile upload with drag-and-drop

**From UX - Layout Templates**
- `OnboardingLayout`: Steps with progress bar
- `DashboardLayout`: Navbar + Tabs + Content
- `AdminLayout`: Navigation admin différente
- `PublicPageLayout`: Sponsorship page (header + content)

**From UX - Critical User Journeys**
- Journey 1: Company Onboarding → First Allocation (TTFA < 10 min) - 8 screens
- Journey 2: Maintainer Claim (< 15 min) - 7 screens
- Journey 3: Monthly Allocation Management
- Journey 4: Admin Payout Run Investigation
- Journey 5: Support/Disputes

**From UX - Accessibility**
- WCAG 2.1 AA compliance required
- Color contrast ratio ≥ 4.5:1 for text, ≥ 3:1 for UI components
- Focus indicators, keyboard navigation, ARIA labels

### FR Coverage Map

| FR | Epic | Description |
|----|------|-------------|
| FR1 | Epic 1 + 2 | Account creation + sign in |
| FR2 | Epic 2 | Create company workspace |
| FR3 | Epic 2 | Invite users |
| FR4 | Epic 2 | Accept invitation |
| FR5 | Epic 2 | Assign roles |
| FR6 | Epic 2 | Switch workspaces |
| FR7 | Epic 2 | Tenant data isolation |
| FR8 | Epic 3 | Set monthly budget |
| FR9 | Epic 3 | Update monthly budget |
| FR10 | Epic 3 | Import npm packages |
| FR11 | Epic 3 | View imported packages |
| FR12 | Epic 4 | Create allocation draft |
| FR13 | Epic 4 | Allocation guardrails |
| FR14 | Epic 4 | Finalize allocation |
| FR15 | Epic 4 | View allocation history |
| FR16 | Epic 4 | Export allocations |
| FR17 | Epic 5 | Enable/disable public page |
| FR18 | Epic 5 | View public company page |
| FR19 | Epic 5 | See funded packages aggregate |
| FR20 | Epic 5 | Private sponsorship view |
| FR21 | Epic 6 | Create maintainer account |
| FR22 | Epic 6 | Create maintainer profile |
| FR23 | Epic 6 | Initiate package claim |
| FR24 | Epic 6 | Track claim verification status |
| FR25 | Epic 6 | View claimed packages |
| FR26 | Epic 6 | Package eligibility status |
| FR27 | Epic 7 | Calculate payout distributions |
| FR28 | Epic 7 | Execute payout run |
| FR29 | Epic 7 | Payout outcome states |
| FR30 | Epic 7 | View payout outcomes (company) |
| FR31 | Epic 7 | Connect payout method |
| FR32 | Epic 7 | View payout history (maintainer) |
| FR33 | Epic 8 | View payout run summaries |
| FR34 | Epic 8 | Retry failed payouts |
| FR35 | Epic 8 | Search by company/package |
| FR36 | Epic 8 | Explain held/unclaimed funds |
| FR37 | Epic 4 | Audit event: budget changes |
| FR38 | Epic 4 | Audit event: allocation finalized |
| FR39 | Epic 7 | Audit event: payout runs |
| FR40 | Epic 8 | Audit event: claim changes |
| FR41 | Epic 8 | Export CSV report |

## Epic List

### Epic 1: Project Foundation & Authentication
**Goal:** Users can create an account, authenticate, and the system is ready for development with hexagonal architecture, containerization, and core infrastructure.

**User Outcome:** A user can sign up, log in, and access a functional application base.

**FRs covered:** FR1 (partial - account creation)

**Implementation Notes:**
- Monorepo setup: `apps/web/` (React + Vite), `apps/api/` (Quarkus), `infra/`
- Hexagonal architecture: domain, application (ports), infrastructure (adapters)
- Docker containerization: web, api, postgres, local dev compose
- CI/CD pipeline basics
- OAuth (GitHub, Google) + email authentication
- Design tokens + shadcn/ui base components
- Base layouts (OnboardingLayout, DashboardLayout, PublicPageLayout)
- API envelope pattern: `{ data, meta }` / `{ error }`

---

### Story 1.1: Monorepo & Hexagonal Architecture Setup

As a **developer**,
I want a monorepo structure with hexagonal architecture scaffolding,
So that I can start implementing features with clear separation of concerns.

**Acceptance Criteria:**

**Given** I clone the repository
**When** I open the project
**Then** I see the following structure:
```
apps/
  web/          # React + Vite + TypeScript
  api/          # Quarkus + Java
infra/          # Docker, CI/CD configs
```
**And** the `apps/api/` follows hexagonal architecture:
```
src/main/java/com/upkeep/
  domain/           # Entities, Value Objects, Domain Services
  application/      # UseCases (ports in), port interfaces (ports out)
  infrastructure/   # Adapters: REST controllers, JPA repos, external services
```
**And** `apps/web/` has a feature-first structure:
```
src/
  features/
  components/
  lib/
  hooks/
```
**And** a root `package.json` with workspace configuration exists
**And** running `npm install` at root installs all dependencies

---

### Story 1.2: Docker Containerization & Local Dev Environment

As a **developer**,
I want a containerized local development environment,
So that I can run the full stack with a single command.

**Acceptance Criteria:**

**Given** Docker and Docker Compose are installed
**When** I run `docker compose up` from the project root
**Then** the following services start:
  - `web` (React dev server on port 5173)
  - `api` (Quarkus dev mode on port 8080)
  - `postgres` (PostgreSQL 16 on port 5432)
**And** the API connects to the PostgreSQL database
**And** the web app can reach the API at `http://localhost:8080`
**And** hot-reload works for both web and api in dev mode
**And** a `.env.example` file documents required environment variables

---

### Story 1.3: CI Pipeline Setup

As a **developer**,
I want automated CI checks on every push,
So that code quality is enforced before merging.

**Acceptance Criteria:**

**Given** I push code to any branch
**When** the CI pipeline runs
**Then** the following checks execute:
  - Lint (ESLint for web, Checkstyle for api)
  - Unit tests (Vitest for web, JUnit for api)
  - Build verification (both apps compile successfully)
**And** the pipeline fails if any check fails
**And** the pipeline completes in under 5 minutes for typical changes
**And** a GitHub Actions workflow file exists at `.github/workflows/ci.yml`

---

### Story 1.4: API Envelope & Error Handling Pattern

As a **developer**,
I want a standardized API response envelope,
So that all endpoints return consistent JSON structures.

**Acceptance Criteria:**

**Given** any API endpoint
**When** the request succeeds
**Then** the response body follows: `{ "data": <payload>, "meta": { ... } }`
**And** HTTP status is 200 or 201

**Given** any API endpoint
**When** the request fails due to validation
**Then** the response body follows: `{ "error": { "code": "VALIDATION_ERROR", "message": "...", "details": [...] } }`
**And** HTTP status is 400

**Given** any API endpoint
**When** the request fails due to authentication
**Then** HTTP status is 401 with error envelope

**Given** any API endpoint
**When** the request fails due to authorization
**Then** HTTP status is 403 with error envelope

**And** a global exception handler maps exceptions to proper error envelopes
**And** a `ApiResponse<T>` wrapper class exists in the infrastructure layer

---

### Story 1.5: User Registration with Email

As a **visitor**,
I want to create an account with my email and password,
So that I can access Upkeep.

**Acceptance Criteria:**

**Given** I am on the registration page
**When** I enter a valid email, password (min 8 chars, 1 uppercase, 1 number), and confirm password
**And** I submit the form
**Then** my account is created
**And** I am redirected to the onboarding flow
**And** I receive a welcome email

**Given** I enter an email that already exists
**When** I submit the form
**Then** I see an error: "An account with this email already exists"

**Given** I enter mismatched passwords
**When** I submit the form
**Then** I see an error: "Passwords do not match"

**Technical Notes:**
- UseCase: `RegisterUserUseCase`
- Domain: `User` entity (id, email, passwordHash, createdAt, accountType)
- Adapter: `UserJpaRepository`, `BcryptPasswordHasher`
- Password stored with bcrypt hashing (NFR7)

---

### Story 1.6: User Login with Email/Password

As a **registered user**,
I want to log in with my email and password,
So that I can access my account.

**Acceptance Criteria:**

**Given** I am on the login page
**When** I enter valid credentials
**And** I submit the form
**Then** I am authenticated
**And** I receive a JWT access token (stored in httpOnly cookie)
**And** I am redirected to the dashboard

**Given** I enter invalid credentials
**When** I submit the form
**Then** I see an error: "Invalid email or password"
**And** no specific field is highlighted (security best practice)

**Given** I am authenticated
**When** my session expires
**Then** I am prompted to log in again

**Technical Notes:**
- UseCase: `AuthenticateUserUseCase`
- JWT with 15min access token, 7d refresh token
- Tokens in httpOnly secure cookies (NFR3)

---

### Story 1.7: OAuth Authentication (GitHub)

As a **visitor**,
I want to sign up or log in using my GitHub account,
So that I can quickly access Upkeep without creating a password.

**Acceptance Criteria:**

**Given** I am on the login or registration page
**When** I click "Continue with GitHub"
**Then** I am redirected to GitHub OAuth consent screen

**Given** I authorize on GitHub
**When** I am redirected back to Upkeep
**Then** if no account exists, one is created with my GitHub email
**And** I am authenticated and redirected to onboarding (new) or dashboard (existing)

**Given** my GitHub email matches an existing email/password account
**When** I complete OAuth
**Then** my accounts are linked
**And** I can log in with either method

**Technical Notes:**
- UseCase: `OAuthLoginUseCase`
- Adapter: `GitHubOAuthAdapter`
- Store: `provider`, `providerUserId` on User entity

---

### Story 1.8: Design System & Base Components

As a **developer**,
I want a design system with tokens and base components,
So that I can build consistent UIs.

**Acceptance Criteria:**

**Given** I am building a UI feature
**When** I import from the component library
**Then** I have access to:
  - Design tokens (colors, spacing, typography, radii, shadows)
  - shadcn/ui components configured with Upkeep theme
  - Base components: Button, Input, Card, Badge, Avatar, Dropdown
**And** all components meet WCAG 2.1 AA contrast requirements (NFR12)
**And** components support keyboard navigation
**And** a Storybook (or equivalent) documents available components

**Technical Notes:**
- TailwindCSS with custom theme in `tailwind.config.ts`
- CSS variables for theming
- shadcn/ui as base component library

---

### Story 1.9: Base Layouts Implementation

As a **developer**,
I want pre-built layout templates,
So that I can quickly scaffold new pages.

**Acceptance Criteria:**

**Given** I am creating a new page
**When** I use `OnboardingLayout`
**Then** I get a centered card layout with progress stepper slot

**Given** I am creating an authenticated page
**When** I use `DashboardLayout`
**Then** I get a layout with:
  - Top navbar with logo, workspace switcher slot, user menu
  - Optional tab navigation
  - Main content area

**Given** I am creating the public sponsorship page
**When** I use `PublicPageLayout`
**Then** I get a layout with:
  - Minimal header with Upkeep branding
  - Hero section slot
  - Content area

**And** all layouts are responsive (desktop-first, tablet/mobile friendly)
**And** layouts handle loading and error states

---

## Epic 2: Company Workspace & Team Management

**Goal:** A company can create its workspace, invite members, and manage roles with full tenant isolation.

### Story 2.1: Create Company Workspace

As a **signed-in user**,
I want to create a company workspace,
So that my team can manage our open-source funding together.

**Acceptance Criteria:**

**Given** I am authenticated and have no company workspace
**When** I complete the company creation form with:
  - Company name (required, 2-100 chars)
  - Company slug (auto-generated from name, editable, unique)
**Then** a company workspace is created
**And** I am assigned the Owner role
**And** I am redirected to the company dashboard

**Given** I enter a slug that already exists
**When** I submit the form
**Then** I see an error: "This URL is already taken"
**And** alternative slugs are suggested

**Technical Notes:**
- UseCase: `CreateCompanyUseCase`
- Domain: `Company` entity (id, name, slug, createdAt)
- Domain: `Membership` entity (userId, companyId, role, joinedAt)
- Adapter: `CompanyJpaRepository`, `MembershipJpaRepository`

---

### Story 2.2: Company Dashboard Shell

As a **company member**,
I want to access my company dashboard,
So that I can see an overview and navigate to features.

**Acceptance Criteria:**

**Given** I am authenticated and belong to a company
**When** I navigate to the dashboard
**Then** I see:
  - Company name in the header
  - Navigation tabs (Overview, Packages, Allocations, Settings)
  - Empty states with CTAs for features not yet set up
**And** the URL reflects my company: `/dashboard`

**Given** I have no company yet
**When** I try to access the dashboard
**Then** I am redirected to company creation flow

**Technical Notes:**
- UseCase: `GetCompanyDashboardUseCase`
- Tenant scoping: all queries filtered by authenticated user's company_id

---

### Story 2.3: Invite User to Company

As a **company Owner**,
I want to invite users to join my company workspace,
So that my team can collaborate on funding allocations.

**Acceptance Criteria:**

**Given** I am an Owner in a company
**When** I go to Settings > Team and click "Invite Member"
**And** I enter an email address and select a role (Owner or Member)
**And** I submit the invitation
**Then** an invitation is created with status PENDING
**And** an email is sent to the invitee with a unique invite link
**And** the invitation appears in the pending invitations list

**Given** I invite an email that already has a pending invitation
**When** I submit
**Then** I see an error: "An invitation is already pending for this email"

**Given** I am a Member (not Owner)
**When** I try to access the invite feature
**Then** I see an error or the feature is hidden

**Technical Notes:**
- UseCase: `InviteUserToCompanyUseCase`
- Domain: `Invitation` entity (id, companyId, email, role, token, status, expiresAt)
- Invitation expires after 7 days

---

### Story 2.4: Accept Company Invitation

As an **invited user**,
I want to accept an invitation to join a company,
So that I can collaborate with the team.

**Acceptance Criteria:**

**Given** I received an invitation email
**When** I click the invite link
**Then** I am taken to the invitation acceptance page showing:
  - Company name
  - Role I'm being invited as
  - Accept / Decline buttons

**Given** I am not logged in
**When** I click Accept
**Then** I am prompted to log in or create an account
**And** after authentication, the invitation is accepted

**Given** I am logged in
**When** I click Accept
**Then** I become a member of the company with the assigned role
**And** I am redirected to the company dashboard
**And** the invitation status changes to ACCEPTED

**Given** the invitation has expired
**When** I click the link
**Then** I see an error: "This invitation has expired"

**Technical Notes:**
- UseCase: `AcceptInvitationUseCase`
- Updates: Membership created, Invitation status → ACCEPTED

---

### Story 2.5: Manage Team Roles

As a **company Owner**,
I want to view and change member roles,
So that I can control access levels in my company.

**Acceptance Criteria:**

**Given** I am an Owner
**When** I go to Settings > Team
**Then** I see a list of all members with:
  - Name, email, role, joined date
  - Action menu for each member

**Given** I click "Change Role" on a Member
**When** I select "Owner"
**Then** their role is updated to Owner
**And** they gain Owner permissions immediately

**Given** I try to demote myself (the only Owner)
**When** I attempt the action
**Then** I see an error: "Cannot remove the last Owner"

**Given** I am a Member
**When** I view the team page
**Then** I can see members but cannot change roles

**Technical Notes:**
- UseCase: `UpdateMemberRoleUseCase`
- Domain rule: Company must always have at least one Owner

---

### Story 2.6: Workspace Switcher

As a **user belonging to multiple companies**,
I want to switch between my company workspaces,
So that I can manage funding for different organizations.

**Acceptance Criteria:**

**Given** I belong to multiple companies
**When** I click on the workspace switcher in the navbar
**Then** I see a dropdown listing all my companies with:
  - Company name
  - My role in each
  - Visual indicator for current workspace

**When** I select a different company
**Then** the dashboard reloads with that company's data
**And** the URL context updates

**Given** I belong to only one company
**When** I view the navbar
**Then** the switcher shows my company name without dropdown

**Technical Notes:**
- UseCase: `GetUserCompaniesUseCase`
- Frontend: Context/state management for current company

---

### Story 2.7: Tenant Data Isolation

As a **platform operator**,
I want strict tenant isolation,
So that company data is never leaked between organizations.

**Acceptance Criteria:**

**Given** I am authenticated as a member of Company A
**When** I make any API request
**Then** only data belonging to Company A is returned
**And** I cannot access, modify, or reference data from Company B

**Given** a developer writes a new query
**When** they access company-scoped data
**Then** the query MUST include `company_id` filter
**And** code review tooling flags queries missing tenant scope

**Given** an attacker tries to access another company's data by manipulating IDs
**When** the API processes the request
**Then** the request fails with 404 (not 403, to avoid enumeration)

**Technical Notes:**
- Implement `TenantContext` holding current company_id
- UseCase base class enforces tenant scoping
- Repository adapters automatically apply tenant filter
- Integration tests verify isolation

---

## Epic 3: Budget & Dependency Import

**Goal:** A company can set its monthly budget and import its npm dependency list.

### Story 3.1: Set Monthly Budget

As a **company Owner**,
I want to set my company's monthly open-source budget,
So that we can start allocating funds to packages.

**Acceptance Criteria:**

**Given** I am an Owner and no budget is set
**When** I go to the Budget section
**Then** I see an empty state prompting me to set a budget

**Given** I am setting the budget
**When** I enter an amount (e.g., €500) and currency (EUR, USD)
**And** I confirm
**Then** the monthly budget is saved
**And** an audit event is recorded (FR37)
**And** I see the budget displayed with BudgetBar component

**Given** a budget exists
**When** I view the Budget section
**Then** I see: total budget, allocated amount, remaining amount

**Technical Notes:**
- UseCase: `SetCompanyBudgetUseCase`
- Domain: `Budget` entity (id, companyId, amountCents, currency, effectiveFrom)
- Domain: `AuditEvent` entity (id, companyId, eventType, payload, actorId, timestamp)
- Store amounts in cents to avoid floating point issues

---

### Story 3.2: Update Monthly Budget

As a **company Owner**,
I want to update my company's monthly budget,
So that I can adjust our open-source spending.

**Acceptance Criteria:**

**Given** a budget exists
**When** I click "Edit Budget" and enter a new amount
**And** I confirm
**Then** the budget is updated
**And** an audit event is recorded
**And** the new amount is reflected in the UI

**Given** I try to set a budget lower than current allocations
**When** I confirm
**Then** I see a warning: "Current allocations exceed this budget. Please adjust allocations first."
**And** I can choose to proceed (allocations become over-budget) or cancel

**Technical Notes:**
- UseCase: `UpdateCompanyBudgetUseCase`
- Budget changes take effect for next allocation period

---

### Story 3.3: Import npm Dependencies via File Upload

As a **company Member**,
I want to upload my lockfile to import dependencies,
So that I can see which packages my company uses.

**Acceptance Criteria:**

**Given** I am on the Packages page
**When** I drag and drop a `package-lock.json` or `yarn.lock` file onto the FileDropzone
**Then** the file is uploaded and parsed
**And** I see a progress indicator during processing (NFR2)
**And** discovered packages are listed with: name, version, count of dependents

**Given** parsing succeeds
**When** the import completes
**Then** packages are added to the company's package list
**And** I see a success message: "Imported X packages"

**Given** the file is invalid or parsing fails
**When** the import fails
**Then** I see an actionable error message
**And** I can retry with a different file

**Technical Notes:**
- UseCase: `ImportPackagesFromLockfileUseCase`
- Domain: `Package` entity (id, name, registry, companyId, importedAt)
- Adapter: `LockfileParserAdapter` (supports package-lock.json v2/v3, yarn.lock)
- Async processing with progress updates via polling or SSE

---

### Story 3.4: Import npm Dependencies via Paste

As a **company Member**,
I want to paste a list of package names,
So that I can quickly add packages without a lockfile.

**Acceptance Criteria:**

**Given** I am on the Packages page
**When** I click "Paste package list"
**And** I paste a newline-separated list of package names
**And** I submit
**Then** each valid package name is added to the company's list
**And** invalid entries are reported with reasons

**Given** I paste packages that already exist in my list
**When** I submit
**Then** duplicates are skipped
**And** I see: "X new packages added, Y already existed"

**Technical Notes:**
- UseCase: `ImportPackagesFromListUseCase`
- Validate package names against npm registry format

---

### Story 3.5: View Package List

As a **company Member**,
I want to view all imported packages,
So that I can see what dependencies my company tracks.

**Acceptance Criteria:**

**Given** packages have been imported
**When** I go to the Packages page
**Then** I see a list of packages displayed with PackageCard component showing:
  - Package name
  - Current allocation (if any)
  - Claim status (claimed/unclaimed)

**Given** I have many packages
**When** I use the search box
**Then** packages are filtered by name in real-time

**Given** no packages exist
**When** I view the page
**Then** I see an EmptyState with CTA to import packages

**Technical Notes:**
- UseCase: `ListCompanyPackagesUseCase`
- Pagination: load 50 packages initially, infinite scroll for more

---

## Epic 4: Allocation Workflow

**Goal:** A company can allocate its monthly budget to packages with anti-capture guardrails.

### Story 4.1: Create Allocation Draft

As a **company Member**,
I want to create a monthly allocation draft,
So that I can propose how to distribute our budget across packages.

**Acceptance Criteria:**

**Given** I am on the Allocations page
**When** I click "New Allocation" for the current month
**Then** a draft allocation is created
**And** I see the AllocationEditor with:
  - List of packages with allocation inputs
  - BudgetBar showing total/allocated/remaining
  - GuardrailBadge indicators

**Given** a draft already exists for this month
**When** I go to Allocations
**Then** I can continue editing the existing draft

**Technical Notes:**
- UseCase: `CreateAllocationDraftUseCase`
- Domain: `AllocationDraft` entity (id, companyId, periodMonth, status, lineItems)
- Domain: `AllocationLineItem` (packageId, amountCents, percentage)

---

### Story 4.2: Edit Allocation with Real-time Guardrails

As a **company Member**,
I want real-time feedback on allocation rules,
So that I know when my allocation is valid.

**Acceptance Criteria:**

**Given** I am editing an allocation draft
**When** I adjust package amounts
**Then** the UI updates in real-time:
  - BudgetBar reflects allocated vs remaining
  - Percentages per package are calculated
  - GuardrailBadge shows rule status

**Given** I allocate to fewer than 3 packages
**When** I view the guardrails
**Then** I see: "⚠️ Minimum 3 packages required" (rule not satisfied)

**Given** I allocate more than 34% to one package
**When** I view the guardrails
**Then** I see: "⚠️ Max 34% per package exceeded" (rule not satisfied)

**Given** all guardrails are satisfied
**When** I view the guardrails
**Then** I see: "✅ Allocation valid" (all rules green)

**Technical Notes:**
- UseCase: `UpdateAllocationDraftUseCase`
- Domain Service: `GuardrailValidator` (MIN_PACKAGES=3, MAX_SHARE_PERCENT=34)
- Frontend: Debounced auto-save of draft changes

---

### Story 4.3: Finalize Allocation

As a **company Owner**,
I want to finalize a monthly allocation,
So that it becomes the official allocation for payouts.

**Acceptance Criteria:**

**Given** I am an Owner viewing a valid allocation draft
**When** I click "Finalize Allocation"
**Then** the draft status changes to FINALIZED
**And** an AllocationSnapshot is created
**And** an audit event is recorded (FR38)
**And** the allocation becomes read-only

**Given** the allocation draft violates guardrails
**When** I try to finalize
**Then** I see an error listing the violated rules
**And** finalization is blocked

**Given** I am a Member (not Owner)
**When** I try to finalize
**Then** I see an error: "Only Owners can finalize allocations"

**Technical Notes:**
- UseCase: `FinalizeAllocationUseCase`
- Domain: `AllocationSnapshot` entity (id, companyId, periodMonth, status=FINALIZED, lineItems, finalizedAt, finalizedBy)
- State transition: DRAFT → FINALIZED (irreversible)

---

### Story 4.4: View Allocation History

As a **company Member**,
I want to view past allocations,
So that I can see how our funding evolved over time.

**Acceptance Criteria:**

**Given** allocations have been finalized
**When** I go to the Allocations page
**Then** I see a list of past allocations with:
  - Month/period
  - Total amount
  - Number of packages
  - Status (FINALIZED)

**When** I click on a past allocation
**Then** I see the full breakdown:
  - Each package with amount and percentage
  - Who finalized it and when

**Given** I use the MonthNavigator
**When** I select a specific month
**Then** the view shows that month's allocation (or empty if none)

**Technical Notes:**
- UseCase: `ListAllocationHistoryUseCase`
- UseCase: `GetAllocationSnapshotUseCase`

---

### Story 4.5: Export Allocations

As a **company Member**,
I want to export allocations as CSV,
So that I can share data with finance or for internal reporting.

**Acceptance Criteria:**

**Given** I am viewing allocations
**When** I click "Export" and select a date range
**Then** a CSV file is generated with columns:
  - Period, Package Name, Amount, Percentage, Status
**And** the file downloads automatically

**Given** no allocations exist for the selected range
**When** I export
**Then** I see a message: "No data for selected period"

**Technical Notes:**
- UseCase: `ExportAllocationsUseCase`
- CSV generation in backend, streamed response

---

## Epic 5: Company Sponsorship Transparency

**Goal:** A company can publish its sponsorship page and showcase its open-source commitment.

### Story 5.1: Toggle Public Sponsorship Page

As a **company Owner**,
I want to enable or disable my public sponsorship page,
So that I control what the public sees about our funding.

**Acceptance Criteria:**

**Given** I am an Owner in Settings > Public Page
**When** I toggle "Make sponsorship page public"
**Then** the setting is saved
**And** I see a preview link to the public page

**Given** the page is disabled
**When** a visitor tries to access `/sponsors/[company-slug]`
**Then** they see a 404 page

**Given** the page is enabled
**When** a visitor accesses the URL
**Then** they see the public sponsorship page

**Technical Notes:**
- UseCase: `UpdateCompanyPublicPageSettingsUseCase`
- Domain: `Company.isPublicPageEnabled` boolean field

---

### Story 5.2: Public Sponsorship Page View

As a **visitor**,
I want to view a company's public sponsorship page,
So that I can see their open-source commitment.

**Acceptance Criteria:**

**Given** the company has enabled their public page
**When** I visit `/sponsors/[company-slug]`
**Then** I see:
  - Company name and logo (if set)
  - "Proudly supporting open source" messaging
  - List of funded packages (names only)
  - Aggregate monthly sponsorship amount
  - Total packages funded count

**Given** the company has no finalized allocations
**When** I view the page
**Then** I see: "No active sponsorships yet"

**Technical Notes:**
- UseCase: `GetPublicSponsorshipPageUseCase`
- No authentication required
- Data aggregated from latest AllocationSnapshot

---

### Story 5.3: Private Sponsorship View

As a **company Member**,
I want to view a detailed sponsorship summary,
So that I can see our funding impact even if public page is disabled.

**Acceptance Criteria:**

**Given** I am a company member
**When** I go to the Sponsorship section in dashboard
**Then** I see:
  - Same data as public page, plus:
  - Detailed breakdown per package
  - Historical monthly totals
  - Link to enable public page (if disabled)

**Technical Notes:**
- UseCase: `GetPrivateSponsorshipSummaryUseCase`
- Reuses components from public page with additional details

---

## Epic 6: Maintainer Onboarding & Package Claiming

**Goal:** A maintainer can sign up, claim their packages, and verify ownership.

### Story 6.1: Maintainer Account Creation

As a **visitor**,
I want to create a maintainer account,
So that I can claim packages and receive payouts.

**Acceptance Criteria:**

**Given** I am on the registration page
**When** I select "I'm a maintainer"
**And** I complete registration (email/password or OAuth)
**Then** my account is created with type=MAINTAINER
**And** I am redirected to the maintainer onboarding flow

**Given** I have an existing company account
**When** I try to create a maintainer account with same email
**Then** I can link both account types to the same user
**And** I can switch between company and maintainer views

**Technical Notes:**
- UseCase: `RegisterMaintainerUseCase`
- Domain: `User.accountType` supports COMPANY, MAINTAINER, or BOTH

---

### Story 6.2: Maintainer Profile Setup

As a **maintainer**,
I want to create my profile,
So that companies can identify who maintains packages.

**Acceptance Criteria:**

**Given** I am a new maintainer
**When** I complete onboarding
**Then** I am prompted to fill my profile:
  - Display name (required)
  - GitHub username (optional, for verification)
  - npm username (optional, for verification)
  - Bio (optional)
  - Profile picture (optional)

**When** I save my profile
**Then** my information is stored
**And** I am redirected to my maintainer dashboard

**Technical Notes:**
- UseCase: `UpdateMaintainerProfileUseCase`
- Domain: `MaintainerProfile` entity (userId, displayName, githubUsername, npmUsername, bio, avatarUrl)

---

### Story 6.3: Initiate Package Claim

As a **maintainer**,
I want to claim an npm package,
So that I can receive funding allocated to it.

**Acceptance Criteria:**

**Given** I am on my maintainer dashboard
**When** I click "Claim a Package"
**And** I enter an npm package name
**Then** the system validates the package exists on npm
**And** a claim is created with status PENDING
**And** I am shown verification options

**Given** I enter a package that doesn't exist
**When** I submit
**Then** I see an error: "Package not found on npm"

**Given** the package is already claimed by another verified maintainer
**When** I submit
**Then** I see: "This package is already claimed. Contact support if you believe this is an error."

**Technical Notes:**
- UseCase: `InitiatePackageClaimUseCase`
- Domain: `PackageClaim` entity (id, maintainerId, packageName, status, createdAt)
- Adapter: `NpmRegistryAdapter` to validate package existence

---

### Story 6.4: Package Ownership Verification

As a **maintainer**,
I want to verify my package ownership,
So that my claim can be approved.

**Acceptance Criteria:**

**Given** I have a pending claim
**When** I view verification options
**Then** I see:
  - Option 1: Connect GitHub and verify collaborator access
  - Option 2: Verify npm publish access (publish a verification token)
  - Option 3: Request manual review

**Given** I choose GitHub verification
**When** I connect my GitHub account
**And** the system detects I have push access to the package's repo
**Then** my claim status changes to VERIFIED
**And** an audit event is recorded (FR40)

**Given** verification fails
**When** I view my claim
**Then** I see the reason and can try another method

**Technical Notes:**
- UseCase: `VerifyPackageClaimUseCase`
- Domain: `ClaimVerification` entity (claimId, method, status, verifiedAt)
- Adapters: `GitHubAdapter`, `NpmRegistryAdapter`
- State: PENDING → VERIFIED | REJECTED

---

### Story 6.5: Maintainer Dashboard with Claimed Packages

As a **maintainer**,
I want to see all my claimed packages,
So that I can track my claims and expected payouts.

**Acceptance Criteria:**

**Given** I am on my maintainer dashboard
**When** I view my packages
**Then** I see a list of claims with:
  - Package name
  - Claim status (PENDING, VERIFIED, REJECTED)
  - If verified: expected payout this month, total received

**Given** I have no claims
**When** I view the dashboard
**Then** I see an EmptyState with CTA to claim packages

**Technical Notes:**
- UseCase: `GetMaintainerDashboardUseCase`
- Aggregates payout expectations from all companies' allocations

---

### Story 6.6: Package Eligibility Status

As a **platform operator**,
I want the system to track package eligibility,
So that payouts only go to verified maintainers.

**Acceptance Criteria:**

**Given** a package has at least one VERIFIED claim
**When** the system checks eligibility
**Then** the package is marked as ELIGIBLE for payouts

**Given** a package has no verified claims
**When** the system checks eligibility
**Then** the package is marked as UNCLAIMED
**And** allocations to this package result in HELD_UNCLAIMED status

**Technical Notes:**
- UseCase: `CheckPackageEligibilityUseCase`
- Domain: `Package.eligibilityStatus` derived from claims
- Used by payout system to determine outcomes

---

## Epic 7: Payout System

**Goal:** The system calculates and executes monthly payments to maintainers.

### Story 7.1: Connect Payout Method

As a **maintainer**,
I want to connect my payout method,
So that I can receive funds.

**Acceptance Criteria:**

**Given** I am a verified maintainer
**When** I go to Settings > Payouts
**Then** I see a prompt to connect Stripe

**When** I click "Connect with Stripe"
**Then** I am redirected to Stripe Connect onboarding
**And** after completion, my Stripe account is linked

**Given** I have connected Stripe
**When** I view payout settings
**Then** I see my connected account status and can disconnect if needed

**Technical Notes:**
- UseCase: `ConnectPayoutMethodUseCase`
- Adapter: `StripeConnectAdapter`
- Domain: `MaintainerPayoutMethod` entity (userId, stripeAccountId, status)

---

### Story 7.2: Calculate Payout Distributions

As a **system**,
I want to calculate how much each maintainer receives,
So that payouts can be executed accurately.

**Acceptance Criteria:**

**Given** a payout run is initiated for period P
**When** the system calculates distributions
**Then** for each finalized allocation in period P:
  - Each package's amount is assigned to its verified maintainer(s)
  - If multiple maintainers, amount is split equally
  - If no verified maintainer, amount is marked HELD_UNCLAIMED

**And** a PayoutLineItem is created for each distribution:
  - companyId, packageName, maintainerId, amountCents, status

**Technical Notes:**
- UseCase: `CalculatePayoutDistributionsUseCase`
- Domain: `PayoutRun` entity (id, periodMonth, status, createdAt)
- Domain: `PayoutLineItem` entity (runId, companyId, packageName, maintainerId, amountCents, status)

---

### Story 7.3: Execute Payout Run

As a **system operator**,
I want to execute a payout run,
So that maintainers receive their payments.

**Acceptance Criteria:**

**Given** distributions are calculated for a payout run
**When** I trigger "Execute Payouts"
**Then** the run status changes to RUNNING
**And** for each PENDING line item:
  - If maintainer has connected Stripe: transfer via Stripe, status → PAID
  - If maintainer has no payout method: status → HELD_UNCLAIMED
  - If Stripe transfer fails: status → FAILED with error reason

**When** all line items are processed
**Then** run status changes to COMPLETED
**And** an audit event is recorded (FR39)

**Given** the run is re-executed
**When** processing occurs
**Then** only PENDING and FAILED items are retried (idempotency - NFR9)

**Technical Notes:**
- UseCase: `ExecutePayoutRunUseCase`
- Adapter: `StripePayoutAdapter`
- State: CREATED → RUNNING → COMPLETED | FAILED
- Implement idempotency keys for Stripe transfers

---

### Story 7.4: Payout Outcome States

As a **platform user**,
I want explicit payout states,
So that I always know what happened to funds.

**Acceptance Criteria:**

**Given** a payout line item
**When** I view its status
**Then** it shows one of:
  - `PAID`: Successfully transferred to maintainer
  - `HELD_UNCLAIMED`: Package has no verified maintainer
  - `FAILED`: Transfer attempted but failed (with reason)
  - `PENDING`: Not yet processed

**And** PayoutStatusBadge component displays the status with appropriate color and icon

**Technical Notes:**
- Domain: `PayoutOutcome` enum with statuses
- Each status has a user-friendly explanation

---

### Story 7.5: Company View Payout Outcomes

As a **company Member**,
I want to see payout outcomes for my allocations,
So that I know where my funding went.

**Acceptance Criteria:**

**Given** I am viewing an allocation
**When** I look at payout status
**Then** each package shows:
  - Amount allocated
  - Payout status (PAID, HELD_UNCLAIMED, FAILED, PENDING)
  - If HELD_UNCLAIMED: explanation "Awaiting maintainer claim"
  - If FAILED: reason for failure

**Given** I view the Allocations overview
**Then** I see aggregate stats:
  - Total paid out
  - Total held (unclaimed)
  - Total failed

**Technical Notes:**
- UseCase: `GetAllocationPayoutStatusUseCase`
- Joins AllocationSnapshot with PayoutLineItems

---

### Story 7.6: Maintainer Payout History

As a **maintainer**,
I want to see my payout history,
So that I can track my earnings.

**Acceptance Criteria:**

**Given** I am on my maintainer dashboard
**When** I go to Payouts section
**Then** I see:
  - Total earnings (all time)
  - This month's expected payout
  - Payout history list with: date, amount, package, status

**When** I click on a payout
**Then** I see details: which companies contributed (aggregate, not individual)

**Technical Notes:**
- UseCase: `GetMaintainerPayoutHistoryUseCase`
- Privacy: Don't expose individual company allocation amounts

---

## Epic 8: Admin & Support Operations

**Goal:** Internal team can monitor, investigate, and resolve payout issues.

### Story 8.1: Admin Payout Run Dashboard

As an **internal operator**,
I want to view payout run summaries,
So that I can monitor the health of the payout system.

**Acceptance Criteria:**

**Given** I am an admin user
**When** I access the Admin dashboard
**Then** I see a list of payout runs with:
  - Period, status, total amount, paid/held/failed counts
  - Run timestamps

**When** I click on a run
**Then** I see detailed breakdown:
  - All line items grouped by status
  - Failed items with error messages
  - Ability to filter/search

**Technical Notes:**
- UseCase: `GetPayoutRunSummaryUseCase`
- Admin role check via separate admin authentication

---

### Story 8.2: Retry Failed Payouts

As an **internal operator**,
I want to retry failed payouts,
So that temporary failures can be resolved.

**Acceptance Criteria:**

**Given** I am viewing a payout run with FAILED items
**When** I select failed items and click "Retry"
**Then** those items are re-processed
**And** status updates based on retry result

**Given** I retry an item that fails again
**When** the retry completes
**Then** the failure count increments
**And** after 3 failures, item is flagged for manual review

**Technical Notes:**
- UseCase: `RetryFailedPayoutsUseCase`
- Track retryCount on PayoutLineItem

---

### Story 8.3: Search by Company or Package

As an **internal support user**,
I want to search by company or package,
So that I can investigate issues quickly.

**Acceptance Criteria:**

**Given** I am in the Admin support view
**When** I search by company name or ID
**Then** I see:
  - Company details
  - All allocations
  - All payout outcomes

**When** I search by package name
**Then** I see:
  - Package details
  - Claim status and history
  - All allocations to this package
  - Payout history

**Technical Notes:**
- UseCase: `SearchCompanyUseCase`, `SearchPackageUseCase`
- Full-text search with filters

---

### Story 8.4: Investigation Timeline

As an **internal support user**,
I want to see a timeline of events,
So that I can understand what happened in a dispute.

**Acceptance Criteria:**

**Given** I am investigating a company or package
**When** I view the Timeline tab
**Then** I see chronological events:
  - Budget changes
  - Allocation finalizations
  - Payout outcomes
  - Claim status changes
**And** each event shows: timestamp, actor, details

**Technical Notes:**
- UseCase: `GetEntityTimelineUseCase`
- Queries AuditEvent table filtered by entity

---

### Story 8.5: Held/Unclaimed Funds Explanation

As a **support user**,
I want clear explanations for held funds,
So that I can help users understand outcomes.

**Acceptance Criteria:**

**Given** a package allocation is HELD_UNCLAIMED
**When** I view the explanation
**Then** I see:
  - "No verified maintainer for this package"
  - Claim history (if any pending claims)
  - Suggested next steps

**Given** I am a company member viewing held funds
**When** I see the status
**Then** I see a user-friendly explanation:
  - "Funds are held until a maintainer claims this package"
  - Option to reallocate next month

**Technical Notes:**
- UseCase: `GetHeldFundsExplanationUseCase`
- Reusable explanations for admin and user views

---

### Story 8.6: Export CSV Report (Admin)

As a **company Member or admin**,
I want to export comprehensive CSV reports,
So that I can reconcile data externally.

**Acceptance Criteria:**

**Given** I am a company member
**When** I export allocations and payouts
**Then** the CSV includes:
  - Allocation data + payout outcomes per line

**Given** I am an admin
**When** I export a payout run
**Then** the CSV includes all line items with full details

**Technical Notes:**
- UseCase: `ExportPayoutReportUseCase`
- Extends FR41 for admin use cases
