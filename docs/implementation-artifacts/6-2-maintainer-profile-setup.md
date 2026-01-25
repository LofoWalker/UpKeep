# Story 6.2: Maintainer Profile Setup

Status: ready-for-dev

## Story

As a **maintainer**, I want to create my profile, so that companies can identify who maintains packages.

## Acceptance Criteria

1. **Given** I am a new maintainer, **When** I complete onboarding, **Then** I fill: display name (required), GitHub/npm username (optional), bio, profile picture.

2. **When** I save profile, **Then** info is stored and I'm redirected to maintainer dashboard.

## Tasks

- [ ] Create `MaintainerProfile` entity
- [ ] Create `UpdateMaintainerProfileUseCase`
- [ ] Create profile setup form
- [ ] Add avatar upload

## Dev Notes

### Domain Model
```java
public class MaintainerProfile {
    private final UserId userId;
    private String displayName;
    private String githubUsername;
    private String npmUsername;
    private String bio;
    private String avatarUrl;
}
```

### Database Schema
```sql
CREATE TABLE maintainer_profiles (
    user_id UUID PRIMARY KEY REFERENCES users(id),
    display_name VARCHAR(100) NOT NULL,
    github_username VARCHAR(100),
    npm_username VARCHAR(100),
    bio TEXT,
    avatar_url VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);
```

### E2E Testing Requirements

**Required Playwright tests for this story:**

- [ ] Create Page Object Model for maintainer profile (`e2e/pages/maintainer-profile.ts`)
- [ ] Test: Maintainer can set up profile with display name
- [ ] Test: GitHub username linking works
- [ ] Test: Profile picture upload works
- [ ] Test: Profile validation errors display correctly

**Test file location:** `apps/web/e2e/maintainer-profile.spec.ts`

### References
- [Source: epics.md#Story-6.2]
- FR22: Maintainer profile

## Dev Agent Record
### Agent Model Used
_To be filled_

