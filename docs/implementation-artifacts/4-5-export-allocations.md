# Story 4.5: Export Allocations

Status: ready-for-dev

## Story

As a **company Member**, I want to export allocations as CSV, so that I can share data with finance.

## Acceptance Criteria

1. **Given** I am viewing allocations, **When** I click "Export" and select date range, **Then** CSV downloads with Period, Package Name, Amount, Percentage, Status.

2. **Given** no allocations for range, **When** I export, **Then** I see "No data for selected period".

## Tasks

- [ ] Create `ExportAllocationsUseCase`
- [ ] Generate CSV in backend with streaming response
- [ ] Create export dialog with date range picker
- [ ] Handle empty results

## Dev Notes

### CSV Format
```csv
Period,Package Name,Amount,Currency,Percentage,Status
2026-01,lodash,5000,EUR,10.00,FINALIZED
2026-01,express,3400,EUR,6.80,FINALIZED
```

### REST Endpoint
```java
@GET
@Path("/export")
@Produces("text/csv")
public Response exportAllocations(
    @QueryParam("from") String from,
    @QueryParam("to") String to
) {
    StreamingOutput stream = outputStream -> {
        try (Writer writer = new OutputStreamWriter(outputStream)) {
            exportUseCase.writeCsv(from, to, writer);
        }
    };
    return Response.ok(stream)
        .header("Content-Disposition", "attachment; filename=allocations.csv")
        .build();
}
```

### E2E Testing Requirements

**Required Playwright tests for this story:**

- [ ] Test: Export button triggers CSV download
- [ ] Test: Date range picker filters export data

**Test file location:** `apps/web/e2e/allocation-history.spec.ts` (extend existing file)

### References
- [Source: epics.md#Story-4.5]
- FR16: Export allocations

## Dev Agent Record
### Agent Model Used
_To be filled_

