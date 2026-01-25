# Story 3.4: Import npm Dependencies via Paste

Status: ready-for-dev

## Story

As a **company Member**, I want to paste a list of package names, so that I can quickly add packages without a lockfile.

## Acceptance Criteria

1. **Given** I click "Paste package list" and paste newline-separated names, **When** I submit, **Then** valid packages are added and invalid entries are reported.

2. **Given** I paste packages that already exist, **When** I submit, **Then** duplicates are skipped with message "X new, Y already existed".

## Tasks

- [ ] Create `ImportPackagesFromListUseCase`
- [ ] Validate package names against npm registry format
- [ ] Create paste dialog UI
- [ ] Handle batch import with results summary

## Dev Notes

### Package Name Validation
```java
private static final Pattern NPM_PACKAGE_NAME = Pattern.compile(
    "^(@[a-z0-9-~][a-z0-9-._~]*/)?[a-z0-9-~][a-z0-9-._~]*$"
);
```

### Frontend Paste Dialog
```tsx
<Dialog>
  <DialogContent>
    <DialogHeader>
      <DialogTitle>Add Packages</DialogTitle>
    </DialogHeader>
    <Textarea 
      placeholder="lodash&#10;express&#10;react&#10;@types/node"
      rows={10}
      {...register('packages')}
    />
    <DialogFooter>
      <Button type="submit">Import Packages</Button>
    </DialogFooter>
  </DialogContent>
</Dialog>
```

### E2E Testing Requirements

**Required Playwright tests for this story:**

- [ ] Test: User can paste package list and import
- [ ] Test: Validation for invalid package names
- [ ] Test: Success message shows count of imported packages

**Test file location:** `apps/web/e2e/package-import.spec.ts` (extend existing file)

### References
- [Source: epics.md#Story-3.4]

## Dev Agent Record
### Agent Model Used
_To be filled_

