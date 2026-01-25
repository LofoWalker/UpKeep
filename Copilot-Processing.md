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

