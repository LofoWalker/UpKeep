# Story 2.1: Create Company Workspace

Status: ready-for-dev

## Story

As a **signed-in user**,
I want to create a company workspace,
so that my team can manage our open-source funding together.

## Acceptance Criteria

1. **Given** I am authenticated and have no company workspace  
   **When** I complete the company creation form with:
   - Company name (required, 2-100 chars)
   - Company slug (auto-generated from name, editable, unique)  
   **Then** a company workspace is created  
   **And** I am assigned the Owner role  
   **And** I am redirected to the company dashboard

2. **Given** I enter a slug that already exists  
   **When** I submit the form  
   **Then** I see an error: "This URL is already taken"  
   **And** alternative slugs are suggested

3. **Given** I enter a company name  
   **When** I view the slug field  
   **Then** it auto-generates from the name (lowercase, hyphens)

## Tasks / Subtasks

- [ ] Task 1: Create Company domain model (AC: #1)
  - [ ] 1.1: Create `Company` entity
  - [ ] 1.2: Create `CompanyId` value object
  - [ ] 1.3: Create `CompanySlug` value object with validation
  - [ ] 1.4: Create `Membership` entity
  - [ ] 1.5: Create `Role` enum (OWNER, MEMBER)

- [ ] Task 2: Create company use case (AC: #1, #2)
  - [ ] 2.1: Create `CreateCompanyUseCase` port interface
  - [ ] 2.2: Implement use case with slug uniqueness check
  - [ ] 2.3: Create `CompanyRepository` port interface
  - [ ] 2.4: Create `MembershipRepository` port interface

- [ ] Task 3: Create infrastructure (AC: #1, #2)
  - [ ] 3.1: Implement `CompanyJpaRepository`
  - [ ] 3.2: Implement `MembershipJpaRepository`
  - [ ] 3.3: Create database migrations
  - [ ] 3.4: Create REST endpoint

- [ ] Task 4: Create frontend (AC: #1, #2, #3)
  - [ ] 4.1: Create company creation page
  - [ ] 4.2: Create form with slug auto-generation
  - [ ] 4.3: Add slug availability check (debounced)
  - [ ] 4.4: Handle redirect on success

## Dev Notes

### Domain Model

```java
// Company Entity
package com.upkeep.domain.model.company;

public class Company {
    private final CompanyId id;
    private String name;
    private final CompanySlug slug;
    private boolean isPublicPageEnabled;
    private final Instant createdAt;
    private Instant updatedAt;

    public static Company create(String name, CompanySlug slug) {
        return new Company(
            CompanyId.generate(),
            name,
            slug,
            false,
            Instant.now(),
            Instant.now()
        );
    }
}

public record CompanyId(UUID value) {
    public static CompanyId generate() {
        return new CompanyId(UUID.randomUUID());
    }
}

public record CompanySlug(String value) {
    private static final Pattern VALID_SLUG = Pattern.compile("^[a-z0-9][a-z0-9-]{1,48}[a-z0-9]$");

    public CompanySlug {
        if (!VALID_SLUG.matcher(value).matches()) {
            throw new ValidationException("Invalid slug format");
        }
    }

    public static CompanySlug fromName(String name) {
        String slug = name.toLowerCase()
            .replaceAll("[^a-z0-9]+", "-")
            .replaceAll("^-|-$", "");
        return new CompanySlug(slug);
    }
}

// Membership Entity
public class Membership {
    private final MembershipId id;
    private final UserId userId;
    private final CompanyId companyId;
    private Role role;
    private final Instant joinedAt;

    public static Membership createOwner(UserId userId, CompanyId companyId) {
        return new Membership(
            MembershipId.generate(),
            userId,
            companyId,
            Role.OWNER,
            Instant.now()
        );
    }
}

public enum Role {
    OWNER,
    MEMBER
}
```

### Use Case

```java
package com.upkeep.application.port.in;

public interface CreateCompanyUseCase {
    CreateCompanyResult execute(CreateCompanyCommand command);

    record CreateCompanyCommand(
        String userId,
        String name,
        String slug
    ) {}

    record CreateCompanyResult(
        String companyId,
        String name,
        String slug
    ) {}
}

// Implementation
@ApplicationScoped
public class CreateCompanyUseCaseImpl implements CreateCompanyUseCase {

    private final CompanyRepository companyRepository;
    private final MembershipRepository membershipRepository;

    @Override
    @Transactional
    public CreateCompanyResult execute(CreateCompanyCommand command) {
        CompanySlug slug = new CompanySlug(command.slug());

        // Check slug uniqueness
        if (companyRepository.existsBySlug(slug)) {
            throw new ConflictException("This URL is already taken");
        }

        // Create company
        Company company = Company.create(command.name(), slug);
        companyRepository.save(company);

        // Create owner membership
        UserId userId = UserId.from(command.userId());
        Membership membership = Membership.createOwner(userId, company.getId());
        membershipRepository.save(membership);

        return new CreateCompanyResult(
            company.getId().toString(),
            company.getName(),
            slug.value()
        );
    }
}
```

### Database Schema

```sql
-- V4__create_companies_table.sql
CREATE TABLE companies (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    slug VARCHAR(50) NOT NULL UNIQUE,
    is_public_page_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX idx_companies__slug ON companies(slug);

-- V5__create_memberships_table.sql
CREATE TABLE memberships (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    company_id UUID NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    role VARCHAR(20) NOT NULL,
    joined_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    UNIQUE(user_id, company_id)
);

CREATE INDEX idx_memberships__user_id ON memberships(user_id);
CREATE INDEX idx_memberships__company_id ON memberships(company_id);
```

### REST Endpoint

```java
@Path("/api/companies")
@Authenticated
public class CompanyResource {

    @POST
    public Response createCompany(@Valid CreateCompanyRequest request, @Context SecurityContext ctx) {
        String userId = ctx.getUserPrincipal().getName();
        
        CreateCompanyResult result = createCompanyUseCase.execute(
            new CreateCompanyCommand(userId, request.name(), request.slug())
        );

        return Response.status(201)
            .entity(ApiResponse.success(result))
            .build();
    }

    @GET
    @Path("/check-slug")
    public Response checkSlugAvailability(@QueryParam("slug") String slug) {
        boolean available = !companyRepository.existsBySlug(new CompanySlug(slug));
        List<String> suggestions = available ? List.of() : generateSuggestions(slug);
        
        return Response.ok(ApiResponse.success(
            new SlugCheckResponse(available, suggestions)
        )).build();
    }
}
```

### Frontend Form

```tsx
// apps/web/src/features/company/components/CreateCompanyForm.tsx
import { useForm } from 'react-hook-form'
import { useDebouncedCallback } from 'use-debounce'

export function CreateCompanyForm() {
  const { register, watch, setValue, formState: { errors } } = useForm()
  const navigate = useNavigate()
  
  const name = watch('name')
  const [slugAvailable, setSlugAvailable] = useState<boolean | null>(null)
  const [suggestions, setSuggestions] = useState<string[]>([])

  // Auto-generate slug from name
  useEffect(() => {
    if (name) {
      const slug = name.toLowerCase()
        .replace(/[^a-z0-9]+/g, '-')
        .replace(/^-|-$/g, '')
      setValue('slug', slug)
      checkSlugAvailability(slug)
    }
  }, [name])

  const checkSlugAvailability = useDebouncedCallback(async (slug: string) => {
    const response = await api.get(`/companies/check-slug?slug=${slug}`)
    setSlugAvailable(response.data.available)
    setSuggestions(response.data.suggestions)
  }, 300)

  const onSubmit = async (data) => {
    const response = await api.post('/companies', data)
    navigate('/dashboard')
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)}>
      <div>
        <Label htmlFor="name">Company Name</Label>
        <Input 
          {...register('name', { required: true, minLength: 2, maxLength: 100 })}
        />
      </div>

      <div>
        <Label htmlFor="slug">URL</Label>
        <div className="flex items-center">
          <span className="text-muted-foreground">upkeep.dev/sponsors/</span>
          <Input {...register('slug')} />
        </div>
        {slugAvailable === false && (
          <p className="text-error text-sm">
            This URL is already taken. Try: {suggestions.join(', ')}
          </p>
        )}
        {slugAvailable === true && (
          <p className="text-success text-sm">Available!</p>
        )}
      </div>

      <Button type="submit" disabled={slugAvailable === false}>
        Create Workspace
      </Button>
    </form>
  )
}
```

### Dependencies on Previous Stories

- Story 1.5: User registration (users table)
- Story 1.6: Authentication (JWT)
- Story 1.9: OnboardingLayout

### E2E Testing Requirements

**Required Playwright tests for this story:**

- [ ] Create Page Object Model for company creation page (`e2e/pages/company.ts`)
- [ ] Test: User can create a company workspace with valid data
- [ ] Test: Slug auto-generation from company name
- [ ] Test: Error displayed when slug is already taken
- [ ] Test: Redirect to dashboard after successful creation

**Test file location:** `apps/web/e2e/company-creation.spec.ts`

### References

- [Source: architecture.md#Data-Architecture] - Entity design
- [Source: epics.md#Story-2.1] - Original acceptance criteria

## Dev Agent Record

### Agent Model Used

_To be filled by dev agent_

### Completion Notes List

_To be filled during implementation_

### Change Log

_To be filled during implementation_

### File List

_To be filled after implementation_

