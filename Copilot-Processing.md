# Copilot Processing

## User Request
1. Fix CI workflow: replace pnpm with npm for web job
2. Add Maven wrapper (mvnw) for API job

## Action Plan

### Phase 1: Fix Web CI (npm)
- [x] Replace pnpm setup with npm in ci.yml

### Phase 2: Add Maven Wrapper
- [x] Generate mvnw files in apps/api
- [x] Make mvnw executable

## Tracking
- Status: Complete

## Summary
1. **CI Workflow**: Replaced pnpm configuration with npm:
   - Uses `actions/setup-node@v4` with npm cache
   - Runs `npm ci`, `npm run lint`, `npm test`, `npm run build`

2. **Maven Wrapper**: Generated in `apps/api/`:
   - `mvnw` (Unix script)
   - `mvnw.cmd` (Windows script)
   - `.mvn/wrapper/maven-wrapper.properties`

Commit these changes and push to trigger the CI.
