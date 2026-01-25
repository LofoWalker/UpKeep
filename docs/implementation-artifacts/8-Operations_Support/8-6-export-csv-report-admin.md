# Story 8.6: Export CSV Report (Admin)

Status: ready-for-dev

## Story

As a **company Member or admin**, I want to export comprehensive CSV reports, so that I can reconcile data externally.

## Acceptance Criteria

1. **Given** I am a company member, **When** I export allocations and payouts, **Then** CSV includes allocation data + payout outcomes per line.

2. **Given** I am an admin, **When** I export a payout run, **Then** CSV includes all line items with full details.

## Tasks

- [ ] Create `ExportPayoutReportUseCase`
- [ ] Extend FR41 for admin use cases
- [ ] Add export button to admin payout run view
- [ ] Stream CSV for large datasets

## Dev Notes

### Admin CSV Format
```csv
Run ID,Period,Company,Package,Maintainer,Amount,Currency,Status,Failure Reason,Retry Count
abc-123,2026-01,Acme Corp,lodash,@maintainer,5000,EUR,PAID,,0
abc-123,2026-01,Acme Corp,express,,3400,EUR,HELD_UNCLAIMED,No verified maintainer,0
```

### Streaming Response
```java
@GET
@Path("/payout-runs/{runId}/export")
@Produces("text/csv")
@RolesAllowed("ADMIN")
public Response exportPayoutRun(@PathParam("runId") String runId) {
    StreamingOutput stream = out -> exportUseCase.writePayoutRunCsv(runId, out);
    return Response.ok(stream)
        .header("Content-Disposition", "attachment; filename=payout-run-" + runId + ".csv")
        .build();
}
```

### E2E Testing Requirements

**Required Playwright tests for this story:**

- [ ] Test: Export CSV button triggers file download
- [ ] Test: CSV file contains expected columns

**Test file location:** `apps/web/e2e/admin-export.spec.ts`

### References
- [Source: epics.md#Story-8.6]
- FR41: Export CSV report

## Dev Agent Record
### Agent Model Used
_To be filled_

