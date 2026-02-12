# Story 5.1: Toggle Public Sponsorship Page

Status: ready-for-dev

## Story

As a **company Owner**, I want to enable/disable my public sponsorship page, so that I control what the public sees.

## Acceptance Criteria

1. **Given** I am an Owner in Settings, **When** I toggle "Make sponsorship page public", **Then** setting is saved and I see preview link.

2. **Given** page is disabled, **When** visitor accesses `/sponsors/[slug]`, **Then** they see 404.

3. **Given** page is enabled, **When** visitor accesses URL, **Then** they see public sponsorship page.

## Tasks

- [ ] Add `isPublicPageEnabled` to Company entity
- [ ] Create `UpdateCompanyPublicPageSettingsUseCase`
- [ ] Create toggle switch in settings UI
- [ ] Add public page route check

## Dev Notes

### Database Migration
```sql
ALTER TABLE companies ADD COLUMN is_public_page_enabled BOOLEAN NOT NULL DEFAULT FALSE;
```


### References
- [Source: epics.md#Story-5.1]
- FR17: Enable/disable public page

## Dev Agent Record
### Agent Model Used
_To be filled_

