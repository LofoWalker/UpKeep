# Story 5.3: Private Sponsorship View

Status: ready-for-dev

## Story

As a **company Member**, I want to view a detailed sponsorship summary, so that I can see our funding impact even if public page is disabled.

## Acceptance Criteria

1. **Given** I am a member, **When** I go to Sponsorship section, **Then** I see: public page data plus detailed breakdown, historical monthly totals, link to enable public page.

## Tasks

- [ ] Create `GetPrivateSponsorshipSummaryUseCase`
- [ ] Create sponsorship dashboard tab
- [ ] Reuse public page components with additional details
- [ ] Add historical chart

## Dev Notes

Reuses components from Story 5.2 with additional private data:
- Per-package amounts (not just names)
- Monthly trend chart
- CTA to enable public page if disabled

### References
- [Source: epics.md#Story-5.3]
- FR20: Private sponsorship view

## Dev Agent Record
### Agent Model Used
_To be filled_

