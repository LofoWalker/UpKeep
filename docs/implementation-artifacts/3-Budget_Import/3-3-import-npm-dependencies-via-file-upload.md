# Story 3.3: Import npm Dependencies via File Upload

Status: ready-for-dev

## Story

As a **company Member**, I want to upload my lockfile to import dependencies, so that I can see which packages my company uses.

## Acceptance Criteria

1. **Given** I am on the Packages page, **When** I drag and drop a `package-lock.json` or `yarn.lock` file, **Then** the file is uploaded, parsed, and packages are listed with progress indicator (NFR2).

2. **Given** parsing succeeds, **When** import completes, **Then** packages are added and I see "Imported X packages".

3. **Given** parsing fails, **When** error occurs, **Then** I see an actionable error message.

## Tasks

- [ ] Create `Package` entity (id, name, registry, companyId, importedAt)
- [ ] Create `ImportPackagesFromLockfileUseCase`
- [ ] Create `LockfileParserAdapter` (package-lock.json v2/v3, yarn.lock)
- [ ] Create FileDropzone component
- [ ] Implement async processing with progress via SSE/polling

## Dev Notes

### Domain Model
```java
public class Package {
    private final PackageId id;
    private final CompanyId companyId;
    private final String name;
    private final String registry; // "npm"
    private final Instant importedAt;
}
```

### Database Schema
```sql
CREATE TABLE packages (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL REFERENCES companies(id),
    name VARCHAR(255) NOT NULL,
    registry VARCHAR(50) NOT NULL DEFAULT 'npm',
    imported_at TIMESTAMP WITH TIME ZONE NOT NULL,
    UNIQUE(company_id, name, registry)
);
```

### FileDropzone Component
```tsx
export function FileDropzone({ onFileAccepted }: Props) {
  const { getRootProps, getInputProps, isDragActive } = useDropzone({
    accept: { 'application/json': ['.json'], 'text/plain': ['.lock'] },
    maxFiles: 1,
    onDropAccepted: (files) => onFileAccepted(files[0]),
  })

  return (
    <div {...getRootProps()} className={cn(
      "border-2 border-dashed rounded-lg p-8 text-center cursor-pointer",
      isDragActive ? "border-primary bg-primary/5" : "border-muted"
    )}>
      <input {...getInputProps()} />
      <UploadIcon className="mx-auto h-12 w-12 text-muted-foreground" />
      <p className="mt-2">Drop your lockfile here or click to browse</p>
      <p className="text-sm text-muted-foreground">package-lock.json or yarn.lock</p>
    </div>
  )
}
```


### References
- [Source: epics.md#Story-3.3]
- NFR2: Import completes within 2 minutes

## Dev Agent Record
### Agent Model Used
_To be filled_

