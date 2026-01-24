# Copilot Processing - Open Source Documentation

## Request Details

**User Request:** Créer l'ensemble de la documentation nécessaire pour open-sourcer le projet Upkeep

**Date:** 2026-01-24

## Action Plan

### Phase 1: Core Documentation
- [x] README.md - Refonte complète avec badges, features, quick start
- [x] CONTRIBUTING.md - Guide de contribution détaillé
- [x] CODE_OF_CONDUCT.md - Code de conduite communautaire
- [x] LICENSE - Licence MIT

### Phase 2: Security & Governance
- [x] SECURITY.md - Politique de sécurité et signalement de vulnérabilités

### Phase 3: Developer Documentation
- [ ] docs/ARCHITECTURE.md - Documentation architecture technique
- [ ] docs/DEVELOPMENT.md - Guide de développement local
- [ ] docs/API.md - Documentation API REST

### Phase 4: GitHub Templates
- [ ] .github/ISSUE_TEMPLATE/bug_report.md
- [ ] .github/ISSUE_TEMPLATE/feature_request.md
- [ ] .github/PULL_REQUEST_TEMPLATE.md
- [ ] .github/FUNDING.yml

## Status

**Current Phase:** 1 - Core Documentation
**Progress:** In Progress

---

# Copilot Processing

## User Request
1. Fix CI workflow: replace pnpm with npm for web job
2. Add Maven wrapper (mvnw) for API job

## Action Plan

### Phase 1: Fix Web CI (npm)
- [x] Replace pnpm setup with npm in ci.yml
- [x] Align CI steps with available web scripts (lint + build)

### Phase 2: Add Maven Wrapper
- [x] Generate mvnw files in apps/api
- [x] Make mvnw executable

### Phase 3: Validation
- [x] Run web: npm ci + lint + build
- [x] Run api: checkstyle + tests + package

## Tracking
- Status: Complete

## Summary
- Updated `.github/workflows/ci.yml`:
  - Web job now uses npm cache + `npm ci` and runs `lint` + `build` (no missing `test` script)
  - API job uses `./mvnw` for checkstyle, tests, and packaging
- Fixed API error mapping to avoid a `ClassCastException` during validation error responses:
  - `ApiError.validationFromApiFieldErrors(...)` now converts API `FieldError` objects into domain `FieldError` objects before delegating to `validation(...)`
