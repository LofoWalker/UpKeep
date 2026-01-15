---
stepsCompleted: [step-01-init, step-02-discovery, step-03-success, step-04-journeys, step-06-innovation, step-07-project-type, step-08-scoping, step-09-functional, step-10-nonfunctional, step-11-complete]
inputDocuments:
  - docs/global-idea.md
workflowType: 'prd'
lastStep: 11
documentCounts:
  productBriefs: 0
  research: 0
  brainstorming: 0
  projectDocs: 1
---

# Product Requirements Document - Upkeep

**Author:** Lofo
**Date:** 2026-01-08

## Executive Summary

Upkeep is a B2B platform that helps companies fund open-source maintainers in a structured, transparent way—based on real dependency risk and explicit allocation decisions (not donations, not popularity rankings).

The problem is systemic: a handful of maintainers keep critical parts of the modern software stack alive, while funding is ad-hoc, emotional, and disconnected from actual enterprise usage. High-profile community discussions (e.g., the “Tailwind financial situation” scandal) are a reminder that the economic dependency is real, but the funding mechanism isn’t.

Upkeep’s V1 focuses on the npm ecosystem. Companies declare the packages they rely on and allocate a monthly budget explicitly across a minimum set of packages, with anti-capture rules (e.g., cap per package, minimum dispersion). Maintainership is opt-in, linked to identifiable humans, and the platform provides traceability and transparency acceptable to enterprises.

A key “wow moment” is visibility: users can consult a company page to see which open-source projects it sponsors, making funding decisions legible and accountable.

### What Makes This Special

Upkeep treats open-source funding as an economic and risk-management mechanism:

- No popularity-based ranking.
- No blind redistribution.
- Explicit allocation that reflects “if this breaks, we’re at risk”.
- Anti-capture constraints to avoid single-sponsor domination.
- Enterprise-acceptable transparency and traceability.
- Maintainer opt-in with clear eligibility expectations.

## Project Classification

**Technical Type:** saas_b2b  
**Domain:** general  
**Complexity:** medium  
**Project Context:** Greenfield - new project

Initial classification signals:
- B2B platform with budgets, allocation workflows, transparency dashboards → `saas_b2b`
- Not inherently regulated (health/fintech/etc.) in the core concept → `general`
- Medium complexity due to trust, payment flows, anti-abuse rules, and transparency requirements

## Success Criteria

### User Success

**Company (Operator: Engineering / DevEx)**
- A company can onboard and start allocating within **< 10 minutes** (create account → set monthly budget → import npm dependencies → allocate across ≥ 3 packages).
- A company can publish (opt-in) a company page showing **which packages it sponsors** within **< 2 minutes**.
- A company can adjust allocations month-to-month with clear guardrails (cap per package, minimum dispersion) without needing complex policy decisions.

**Maintainer**
- A maintainer can onboard and claim at least one eligible package within **< 15 minutes** (account → verify package ownership → connect payouts).
- A maintainer can see expected monthly payout breakdown by package and historical payouts.

### Business Success

**3-month targets (V1 traction)**
- ≥ **10** companies onboarded that successfully allocate a budget at least once.
- ≥ **50** distinct npm packages funded.
- ≥ **20** maintainers receiving at least one payout.
- ≥ **€2k/month** total budget allocated (or equivalent target currency).

**12-month targets (product-market signal)**
- ≥ **200** companies onboarded.
- ≥ **1,000** packages funded.
- ≥ **300** maintainers paid.
- ≥ **€100k/month** total budget allocated.

### Technical Success

- Payouts run on a predictable cadence (e.g., monthly) with **no silent failures** (every payout run produces a clear status and audit trail).
- Data integrity: allocation totals always reconcile to the company’s monthly budget.
- Basic anti-abuse enforcement in product rules:
  - min 3 packages allocated
  - max 34% per package
- Security baseline appropriate for B2B:
  - secure auth
  - least-privilege access model (single-company workspace in MVP; teams/RBAC later)
  - auditability of key money-moving actions (who changed budget, who changed payouts)

### Measurable Outcomes

- Median time-to-first-allocation (TTFA): **≤ 10 minutes**
- % onboarded companies that complete first allocation: **≥ 60%**
- Monthly retention (companies that re-allocate next month): **≥ 40%** by month 3
- Payout run success rate: **≥ 99%** (manual intervention allowed, but never “unknown state”)

## Product Scope

### MVP - Minimum Viable Product

**Company**
- Create company workspace
- Set monthly budget
- Import npm dependency list (file upload or paste)
- Allocate budget across packages with constraints (min 3, max 34% per package)
- Company sponsorship page:
  - default private
  - opt-in public (shows list of funded packages + aggregate monthly amount)
- Allocation history (monthly snapshots)
- Simple export (CSV) of allocations and payouts for internal reporting

**Maintainer**
- Signup + basic profile
- Claim package ownership (simple verification flow)
- Connect payouts
- View payouts and history

**Platform**
- Eligibility rules (opt-in maintainers, basic activity/visibility flags)
- Monthly payout run (simple, predictable)
- Minimal admin tooling for dispute/claim issues

### Growth Features (Post-MVP)

- GitHub App integration to auto-sync dependencies from repos/orgs
- Teams + RBAC + approval workflow inside a company
- Package “health signals” (activity, bus factor proxies, security advisories) presented as guidance (not as an opaque ranking)
- Multi-currency + invoicing workflows
- Public aggregates (ecosystem dashboards) without leaking sensitive info

### Vision (Future)

- Standardized “Open Source spend” workflows for enterprises (policy, budgeting, compliance)
- Rich risk modeling (SBOM-aware, dependency criticality, incident response)
- Governance options and ecosystem standards participation
- Ecosystem expansion beyond npm (PyPI, Maven, Go, etc.)

## User Journeys

**Journey 1: Alex Martin (Engineering Manager) — “Stop feeling guilty, start funding responsibly”**
Alex manages a small platform team. They rely on a dense npm ecosystem and have seen enough supply-chain incidents to know the risk is real, but open-source funding inside the company has always been ad-hoc. After the latest community discussion about maintainer sustainability, Alex decides to turn that vague concern into a repeatable company habit.

Alex signs up for Upkeep, sets a modest monthly budget, and uploads a lockfile. Instead of a moral plea or a popularity leaderboard, Upkeep frames the decision as risk management: “these are dependencies you run in production; choose which ones you want to keep healthy.” Alex allocates across at least three packages, hits the cap guardrails, and makes quick tradeoffs. The product feels more like budgeting than donating.

The moment of relief comes when Alex shares an internal link to a single, legible page: “here’s our open-source budget, here’s where it goes.” Next month, reallocating takes minutes, not meetings. Alex feels like they’ve turned an uncomfortable topic into a lightweight, repeatable workflow.

This journey reveals requirements for:
- Company onboarding, budget setup, dependency import
- Allocation UX with guardrails (min dispersion, cap per package)
- Allocation history snapshots and shareable views (private by default)

**Journey 2: Alex Martin (Engineering Manager) — “The allocation fails because the maintainer hasn’t claimed the package”**
Alex has allocated budget to five packages. A month later, they check the payout summary and notice one package hasn’t paid out to any maintainer. The fear: “is the money lost?” The expectation: “I need a clear answer and a clear next action.”

Upkeep shows a transparent status: the package is eligible, but no maintainer has claimed payouts yet. Alex can see that the funds are queued (or held) and can choose a policy: keep allocated for the next cycle, reallocate next month, or redirect to a fallback pool (if enabled later). Alex also sees a non-invasive prompt that the maintainer can opt in to claim the package.

The emotional peak is not disappointment—it’s trust. Even when the system can’t magically create maintainers, it can be explicit about what happened, what will happen next, and what Alex can do.

This journey reveals requirements for:
- Payout status per package (claimed vs unclaimed)
- Clear handling of “unclaimed funds” in MVP (hold + explain, no surprises)
- Audit trail for allocation → payout outcomes

**Journey 3: Lina Dupont (Open-Source Maintainer) — “Claim my package without bureaucracy”**
Lina maintains a widely used npm package. Sponsors exist, but they’re unpredictable, and the administrative overhead is exhausting. Lina wants something that feels like income infrastructure, not begging.

Lina finds Upkeep via a company page or ecosystem discovery. She signs up, links her payout method, and starts the claim flow. Upkeep asks for a simple proof of ownership: connect a GitHub account and complete a lightweight verification step that doesn’t require corporate-style paperwork.

Once verified, Lina can see that multiple companies have allocated budget to her package. The first payout isn’t massive, but it’s recurring, explainable, and doesn’t depend on a single sponsor’s whims. Lina’s “aha moment” is realizing she can focus on maintenance and still have a predictable baseline.

This journey reveals requirements for:
- Maintainer onboarding + payout connection
- Package claim/verification flow
- Maintainer dashboard (package status, expected payouts, history)

**Journey 4: Samira Benali (Platform Ops / Admin) — “Run payouts, trust the ledger”**
Samira runs the operational side. She knows that money-moving systems fail in messy, real-world ways: partial payouts, retries, disputes, blocked accounts. For Upkeep to be enterprise-friendly, it needs operational clarity even if the MVP is simple.

On payout day, Samira triggers (or monitors) the monthly payout run. The system produces a run summary: total budget processed, packages paid, packages held (unclaimed), failures with reasons, and a deterministic audit trail. Samira can safely retry failures, and nothing ends up in an unknown state.

This journey reveals requirements for:
- Payout run orchestration (scheduled or manual trigger)
- Run logs / audit events visible in an internal admin view
- Failure handling: retries, clear statuses, no silent errors

**Journey 5: Jules (Customer Support) — “Resolve a dispute without improvising”**
A company opens a ticket: “We think we funded X but the maintainer says they received nothing.” Jules needs to answer with facts, not guesswork. They search by company and package, see the allocation history, and see the payout status. The explanation is clear: the package wasn’t claimed that month; funds were held; the maintainer claimed later; payouts will execute next run.

Jules replies with a crisp timeline and recommended next step. The customer feels heard, and Upkeep feels reliable.

This journey reveals requirements for:
- Support tooling (search by company/package, view timelines)
- Customer-visible explanations for payout outcomes
- Dispute-friendly audit trail

### Journey Requirements Summary

Core capability areas implied by journeys:
- Authentication + company workspace
- Dependency import (npm lockfile / paste list)
- Budget allocation with guardrails + monthly snapshots
- Company sponsorship page (private by default, opt-in public)
- Maintainer onboarding + claim verification + payouts connection
- Payout run engine + transparent statuses (paid/held/failed)
- Audit trail + basic admin/support tooling

## Innovation & Novel Patterns

### Detected Innovation Areas

Upkeep’s innovation is not “a new payment rail” but a new operational model for open-source funding that enterprises can adopt:

- **Funding as risk management (not charity):** allocations express “if this fails, we’re exposed”, making the decision legible and repeatable.
- **Constrained allocation instead of free-form sponsorship:** guardrails (minimum dispersion, cap per package) reduce single-sponsor capture and encourage ecosystem-wide support.
- **Trust-first transparency:** an enterprise-friendly transparency model (private by default, optional public aggregates) turns sponsorship into accountability without forcing companies into uncomfortable disclosure.
- **Clear states for money movement:** explicit outcomes (paid / held-unclaimed / failed) reduce ambiguity and build trust in the system even when maintainers haven’t opted in yet.

### Market Context & Competitive Landscape

Existing options (GitHub Sponsors, OpenCollective) are strong for individual/community-driven patronage but weak for enterprise workflows because they usually lack:

- a dependency-driven framing,
- repeatable budget allocation mechanics,
- guardrails against capture,
- enterprise-grade traceability of outcomes.

Upkeep positions itself as the “operations layer” for open-source spend.

### Validation Approach

Validate the innovation by proving three things early:

1) **Adoption friction is low:** companies can import npm deps + allocate within minutes (TTFA).
2) **The workflow repeats:** a meaningful fraction of companies re-allocate month-to-month (retention proxy).
3) **Trust survives the hard cases:** unclaimed packages, failed payouts, disputes produce clear outcomes and actions (no ambiguous states).

### Risk Mitigation

Key innovation risks and mitigations:

- **Risk:** companies won’t allocate unless maintainers are already onboarded  \
  **Mitigation:** show “held/unclaimed” states clearly; allow easy reallocation next cycle; actively drive maintainer claim flows.

- **Risk:** public transparency reduces adoption  \
  **Mitigation:** private-by-default, opt-in public pages, aggregate-only disclosure by default.

- **Risk:** guardrails feel arbitrary  \
  **Mitigation:** explain rules in-product (“anti-capture, ecosystem health”) and keep them simple in V1.

## SaaS B2B Specific Requirements

### Project-Type Overview

Upkeep is a multi-tenant B2B SaaS that enables companies to allocate a monthly open-source budget across npm packages with guardrails and full traceability, while enabling maintainers to claim packages and receive payouts.

The system must balance:
- low onboarding friction (fast adoption),
- enterprise acceptability (auditability and access control),
- trustworthy money movement (clear payout states and outcomes).

### Technical Architecture Considerations

- **Multi-tenant by design:** all core data entities are scoped to a `company_id` (company workspace). Tenant isolation is mandatory for confidentiality.
- **Money-moving operations require auditability:** any action impacting budgets, allocations, payouts, or claims must produce an auditable event trail.
- **Deterministic state machines over “best effort”:** payout runs and claim flows should have explicit states; no silent or ambiguous outcomes.
- **Composable ingestion:** dependency import must work without deep integrations (file upload / paste list) and be extensible to GitHub App later.

### Tenant Model

- **Tenant unit:** one company workspace = one tenant.
- **Membership model (MVP):** a company can have multiple users.
- **Isolation requirements:**
  - Users can only view/edit data for companies they belong to.
  - Public sponsorship pages (opt-in) must not leak internal data (e.g., dependency graphs, repos, allocation reasons).

### RBAC / Permissions (MVP)

Role-based access control in V1 is intentionally minimal:

- **Owner**
  - manage company settings
  - set monthly budget
  - finalize allocations
  - enable/disable public company sponsorship page
  - manage payouts-related settings if needed (e.g., billing contact)

- **Member**
  - import dependencies
  - draft allocation proposals
  - view allocation + payout history

Notes:
- No custom roles in V1.
- Any action that changes money movement (budget/allocation/payout) must be restricted to Owner and audited.

### Subscription Tiers / Billing

V1 assumption: **free/private beta** to maximize iteration speed and reduce procurement friction.

Billing decisions deferred to post-MVP, with likely paths:
- small platform fee (flat or percentage) paid by companies, or
- optional “pro” features (RBAC, approvals, integrations, reporting).

### Integrations

MVP integrations are intentionally minimal:
- Dependency import via lockfile upload or paste list.
- CSV export for internal reporting.

Post-MVP integrations:
- GitHub App (sync dependencies from repos/orgs)
- SSO (SAML/OIDC) for enterprise plans
- Finance exports / accounting integrations

### Compliance Requirements (Baseline B2B)

Not formal “regulated domain” compliance, but baseline enterprise expectations:

- **Access control:** tenant isolation + role checks on every sensitive endpoint.
- **Audit trail:** immutable history for:
  - budget changes
  - allocation snapshots
  - payout runs and outcomes
  - maintainer claims + verification events
- **Data retention:** define retention and deletion behavior for company data and payout history.
- **Security posture (MVP):**
  - secure authentication
  - secrets management
  - payment provider handled by a specialized provider (e.g., Stripe Connect)

### Implementation Considerations

- Start with the smallest reliable primitives:
  - lockfile ingestion → normalized package entities
  - allocation engine with guardrails
  - payout state machine (paid/held/failed)
  - audit events
- Keep “enterprise heaviness” (SAML, approvals, advanced RBAC, compliance certifications) out of MVP but design paths for them.

## Project Scoping & Phased Development

### MVP Strategy & Philosophy

**MVP Approach:** Experience MVP (deliver the key enterprise-friendly workflow end-to-end with minimal integrations)

**Resource Requirements:** Solo dev / small team viable.
- 1 fullstack engineer
- part-time product/ops for early adopter onboarding and payouts support

Rationale: Upkeep wins if the end-to-end loop is real and repeatable:
import deps → allocate budget with guardrails → publish/share sponsorship stance → run payouts → handle “held/unclaimed” and disputes transparently.

### MVP Feature Set (Phase 1)

**Core User Journeys Supported:**
- Company operator onboarding + first allocation (Journey 1)
- Company operator “unclaimed package” clarity (Journey 2)
- Maintainer claim + payout onboarding (Journey 3)
- Internal ops payout runs (Journey 4)
- Support investigation + dispute handling (Journey 5)

**Must-Have Capabilities:**
- Authentication + company workspace (multi-tenant via `company_id`)
- Minimal RBAC: Owner / Member
- Monthly budget setting
- Dependency import (lockfile upload + paste list)
- Allocation UX with constraints:
  - min 3 packages
  - max 34% per package
- Allocation snapshots per month + history
- Maintainer signup + package claim verification + payout connection
- Payout run engine with explicit states:
  - paid
  - held (unclaimed)
  - failed
- Audit trail for budget/allocation/payout/claim events
- Company sponsorship page:
  - private by default
  - opt-in public aggregate view
- Basic admin/support tooling to search by company/package and view timelines
- CSV export for company reporting

**Explicitly Out of Scope (MVP):**
- GitHub App auto-sync
- SSO (SAML/OIDC)
- Approval workflows
- Advanced RBAC / custom roles
- Multi-currency, invoicing, complex tax handling
- Package risk scoring or ranking algorithms

### Post-MVP Features

**Phase 2 (Post-MVP):**
- GitHub App integration (sync deps automatically)
- Teams + RBAC expansion + approvals
- Improved discovery and onboarding loops for maintainers to claim packages
- Optional public ecosystem aggregates (without leaking sensitive data)

**Phase 3 (Expansion):**
- Multi-ecosystem support (PyPI, Maven, Go)
- Compliance upgrades for enterprise deals (SSO, audit exports, certs)
- Risk modeling (SBOM-aware criticality, advisories)

### Risk Mitigation Strategy

**Technical Risks:**
- Money movement complexity → Mitigate by using a mature payment provider and strict state machines.
- Identity/claims disputes → Mitigate with a simple verification flow + internal admin tooling and audit trails.

**Market Risks:**
- Chicken-and-egg (companies won’t allocate without maintainers; maintainers won’t onboard without money) → Mitigate with:
  - “held/unclaimed” transparent states
  - maintainer claim funnels
  - onboarding a curated set of maintainers early

**Resource Risks:**
- If resources are tight, keep MVP as a private beta with highly manual ops (admin tooling + clear states) and delay all integrations.

## Functional Requirements

### Identity, Workspaces & Access Control

- FR1: A visitor can create an account and sign in to Upkeep.
- FR2: A signed-in user can create a company workspace.
- FR3: A company Owner can invite a user to join their company workspace.
- FR4: An invited user can accept an invitation and join a company workspace.
- FR5: A company Owner can assign a role to a company member (Owner or Member).
- FR6: A signed-in user can switch between company workspaces they belong to (if applicable).
- FR7: The system restricts all company data access to members of that company workspace.

### Company Budgeting & Allocation

- FR8: A company Owner can set a monthly open-source budget for their company.
- FR9: A company Owner can update the monthly budget.
- FR10: A company Member can import a list of npm packages into the company workspace.
- FR11: A company Member can view the imported package list.
- FR12: A company Member can create a monthly allocation draft across packages.
- FR13: The system enforces allocation guardrails (minimum number of packages; maximum share per package).
- FR14: A company Owner can finalize a monthly allocation.
- FR15: A company member can view monthly allocation history.
- FR16: A company member can export allocations for a given period.

### Sponsorship Transparency (Company Page)

- FR17: A company Owner can enable or disable a public sponsorship page for their company.
- FR18: A visitor can view a public company sponsorship page when enabled.
- FR19: A visitor can see the list of funded packages and an aggregate monthly amount on a public company sponsorship page.
- FR20: A company member can view a private sponsorship view even when public sharing is disabled.

### Maintainer Onboarding, Package Claiming & Eligibility

- FR21: A visitor can create a maintainer account.
- FR22: A maintainer can create a maintainer profile.
- FR23: A maintainer can initiate a package claim for an npm package.
- FR24: The system can record and track the claim verification status for a package.
- FR25: A maintainer can view which packages they have claimed and their claim statuses.
- FR26: The system can represent whether a package has any eligible maintainer(s) for payouts.

### Payouts & Money Movement

- FR27: The system can calculate payout distributions per package based on finalized company allocations.
- FR28: The system can execute a payout run on a defined cadence.
- FR29: The system can represent payout outcomes per package as explicit states (e.g., paid, held/unclaimed, failed).
- FR30: A company member can view payout outcomes for allocations they made.
- FR31: A maintainer can connect a payout method to receive funds.
- FR32: A maintainer can view payout history and expected payouts by package.

### Operations, Support & Dispute Handling

- FR33: An internal operator can view payout run summaries (totals, paid, held, failed).
- FR34: An internal operator can retry failed payouts.
- FR35: An internal support user can search by company and/or package to investigate allocation and payout history.
- FR36: The system can provide a clear explanation for held/unclaimed package funds.

### Auditability & Traceability

- FR37: The system records an audit event when a company budget is created or updated.
- FR38: The system records an audit event when an allocation is finalized.
- FR39: The system records an audit event for payout run execution and outcomes.
- FR40: The system records an audit event for package claim and claim verification changes.

### Reporting & Exports

- FR41: A company member can export a CSV report of allocations and payouts for a given time range.

## Non-Functional Requirements

### Performance

- NFR1: Core interactive pages (sign-in, workspace selection, allocation editing, payout outcomes) return primary content within **2 seconds** for p95 under expected MVP load.
- NFR2: Import processing provides user-visible progress and completes within **2 minutes** for a typical lockfile (or clearly reports failure with actionable guidance).

### Security

- NFR3: All network traffic uses TLS (HTTPS) for all user-facing and API endpoints.
- NFR4: Sensitive data is encrypted at rest.
- NFR5: Strong tenant isolation: company-scoped data is only accessible to members of that company workspace.
- NFR6: All money-impacting actions (budget changes, allocation finalization, payout run execution, claim verification changes) are authenticated, authorized, and recorded in an immutable audit trail.
- NFR7: Authentication supports secure password storage and account recovery flows.
- NFR8: The platform does not store raw payment instrument details; payments/payouts are delegated to a specialized provider.

### Reliability

- NFR9: Payout runs are repeatable and idempotent: re-running a payout run does not double-pay.
- NFR10: The system is never in an “unknown money state”: each package allocation for each period ends in an explicit outcome (paid / held-unclaimed / failed) with an explanation.

### Scalability

- NFR11: Architecture supports at least **10x** growth in number of companies and packages without fundamental redesign (horizontal scaling acceptable).

### Accessibility

- NFR12: Public and authenticated web UI meets **WCAG 2.1 AA** for core flows in MVP (sign-in, allocation, public company page).

### Observability (Ops)

- NFR13: Operational users can observe payout runs via structured logs/metrics and correlate events by company/package/run identifiers.
- NFR14: Error reporting and alerting exist for payout failures and claim verification failures.
