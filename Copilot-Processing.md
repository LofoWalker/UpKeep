# Copilot Processing - E2E Tests with Playwright

## User Request
Implement E2E tests with Playwright framework for the Upkeep monorepo.

## Analysis
- **Frontend**: React app with Vite at `apps/web`
- **Backend**: Quarkus API at `apps/api`
- **Structure**: npm workspaces monorepo

## Action Plan

### Phase 1: Playwright Setup
- [x] Install Playwright in apps/web workspace
- [x] Configure Playwright for the project
- [x] Set up test directory structure

### Phase 2: Configuration
- [x] Create playwright.config.ts
- [x] Configure base URL and web server
- [x] Set up test environment

### Phase 3: Test Infrastructure
- [x] Create test utilities and helpers
- [x] Create page object models (POM) structure
- [x] Add example E2E test

### Phase 4: Scripts Integration
- [x] Add npm scripts for E2E testing
- [x] Update root package.json with e2e commands

## Progress Tracking
- Status: ✅ COMPLETE

## Summary

Successfully implemented Playwright E2E testing framework:

### Files Created
- `apps/web/playwright.config.ts` - Playwright configuration
- `apps/web/e2e/pages/index.ts` - Page Object Models (HomePage, LoginPage, RegisterPage)
- `apps/web/e2e/fixtures/index.ts` - Test fixtures with page object injection
- `apps/web/e2e/app.spec.ts` - Example E2E tests (7 tests, all passing)

### Scripts Added
- `npm run test:e2e` - Run E2E tests
- `npm run test:e2e:ui` - Run tests with Playwright UI
- `npm run test:e2e:debug` - Debug mode
- `npm run test:e2e:report` - Show HTML report

### Test Results
```
7 passed (1.8s)
```
- Added Inter (sans) and JetBrains Mono (mono) fonts
- Defined spacing, radius, and shadow scales

### UI Components (shadcn/ui compatible)
- **Button** - With variants: default, destructive, outline, secondary, ghost, link
- **Input** - Base input with focus styles
- **FormInput** - Input wrapper with label and error support
- **Card** - With CardHeader, CardTitle, CardDescription, CardContent, CardFooter
- **Badge** - With variants: default, secondary, destructive, outline, success, warning
- **Avatar** - With AvatarImage and AvatarFallback
- **DropdownMenu** - Full dropdown with items, separators, labels, checkboxes
- **Dialog** - Modal dialog with header, content, footer
- **Alert** - With variants: default, destructive, success, warning
- **Toast** - Toast notification system with useToast hook
- **Label** - Accessible label component
- **Separator** - Horizontal/vertical divider

### Custom Components
- **LoadingSpinner** - Animated spinner with size variants and optional message
- **ErrorBoundary** - React error boundary with fallback UI

### Storybook
- Configured with react-vite framework
- Added accessibility addon (@storybook/addon-a11y)
- Created stories for all components
- Run with: `npm run storybook`

### Dependencies Added
- @radix-ui/react-* (avatar, dialog, dropdown-menu, label, separator, slot, toast)
- class-variance-authority, clsx, tailwind-merge
- lucide-react (icons)
- tailwindcss-animate
- @storybook/* (v8)

**Note:** Please review and remove this file when done.
12. `callback_whenUnexpectedErrorOccurs_shouldRedirectWithServerError` - Unexpected error handling
13. `callback_withMaintainerAccountType_shouldCreateMaintainerAccount` - Maintainer creation

**File Created:**
- `apps/api/src/test/java/com/upkeep/infrastructure/adapter/in/rest/auth/OAuthResourceTest.java`

**Key Test Patterns Used:**
- `@QuarkusTest` for integration testing
- `@InjectMock` with `@Named("github")` for mocking the GitHub provider adapter
- RestAssured for HTTP endpoint testing
- Redirect following disabled to test 307 responses and cookies
- `application.properties` - Added `%dev.app.cookie-domain=localhost`

**To test:** Restart the backend server and try the GitHub OAuth flow again.

### Issue 4: INVALID_TOKEN error on /api/auth/me
**Root cause**: The `validateAccessToken()` method in `JwtTokenService` was throwing `UnsupportedOperationException` - it was never implemented!

**Solution**: 
- Injected `JWTParser` from SmallRye JWT
- Implemented `validateAccessToken()` to parse the JWT and extract claims (subject, email, accountType)
- The method now properly validates and decodes the access token from the cookie

**Files modified:**
- `JwtTokenService.java` - Added JWTParser injection and implemented token validation

**To test:** Restart the backend server and try the GitHub OAuth flow again.

