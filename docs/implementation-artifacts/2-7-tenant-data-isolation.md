# Story 2.7: Tenant Data Isolation

Status: ready-for-dev

## Story

As a **platform operator**,
I want strict tenant isolation,
so that company data is never leaked between organizations.

## Acceptance Criteria

1. **Given** I am authenticated as a member of Company A  
   **When** I make any API request  
   **Then** only data belonging to Company A is returned  
   **And** I cannot access, modify, or reference data from Company B

2. **Given** a developer writes a new query  
   **When** they access company-scoped data  
   **Then** the query MUST include `company_id` filter  
   **And** code review tooling flags queries missing tenant scope

3. **Given** an attacker tries to access another company's data by manipulating IDs  
   **When** the API processes the request  
   **Then** the request fails with 404 (not 403, to avoid enumeration)

## Tasks / Subtasks

- [ ] Task 1: Create TenantContext (AC: #1)
  - [ ] 1.1: Create `TenantContext` to hold current company_id
  - [ ] 1.2: Create request filter to populate context
  - [ ] 1.3: Create `@TenantScoped` annotation

- [ ] Task 2: Implement repository scoping (AC: #1, #3)
  - [ ] 2.1: Create `TenantAwareRepository` base class
  - [ ] 2.2: Apply tenant filter to all queries
  - [ ] 2.3: Return 404 for unauthorized access attempts

- [ ] Task 3: Create validation tests (AC: #1, #2, #3)
  - [ ] 3.1: Write integration tests for isolation
  - [ ] 3.2: Test cross-tenant access prevention
  - [ ] 3.3: Create architecture test for tenant scoping

## Dev Notes

### TenantContext Implementation

```java
package com.upkeep.infrastructure.tenant;

@RequestScoped
public class TenantContext {
    private CompanyId companyId;

    public CompanyId getCompanyId() {
        return companyId;
    }

    public void setCompanyId(CompanyId companyId) {
        this.companyId = companyId;
    }

    public void requireTenant() {
        if (companyId == null) {
            throw new IllegalStateException("Tenant context not set");
        }
    }
}
```

### Request Filter

```java
@Provider
@Priority(Priorities.AUTHENTICATION + 1)
public class TenantContextFilter implements ContainerRequestFilter {

    @Inject
    TenantContext tenantContext;

    @Inject
    MembershipRepository membershipRepository;

    @Override
    public void filter(ContainerRequestContext ctx) {
        // Skip for public endpoints
        if (isPublicEndpoint(ctx)) return;

        String userId = ctx.getSecurityContext().getUserPrincipal().getName();
        String companyId = extractCompanyIdFromPath(ctx);

        if (companyId != null) {
            // Verify user is member of company
            UserId uid = UserId.from(userId);
            CompanyId cid = CompanyId.from(companyId);

            boolean isMember = membershipRepository.existsByUserAndCompany(uid, cid);
            if (!isMember) {
                // Return 404, not 403 (prevents enumeration)
                ctx.abortWith(Response.status(404)
                    .entity(ApiResponse.error(ApiError.of("NOT_FOUND", "Resource not found", UUID.randomUUID().toString())))
                    .build());
                return;
            }

            tenantContext.setCompanyId(cid);
        }
    }

    private String extractCompanyIdFromPath(ContainerRequestContext ctx) {
        // Extract from /api/companies/{companyId}/... paths
        String path = ctx.getUriInfo().getPath();
        Pattern pattern = Pattern.compile("/companies/([^/]+)");
        Matcher matcher = pattern.matcher(path);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}
```

### TenantAwareRepository Base

```java
package com.upkeep.infrastructure.adapter.out.persistence;

public abstract class TenantAwareRepository<T, ID> {

    @Inject
    TenantContext tenantContext;

    protected CompanyId requireTenantId() {
        tenantContext.requireTenant();
        return tenantContext.getCompanyId();
    }

    // All queries must be scoped
    protected String addTenantFilter(String jpql) {
        // Ensure company_id is in WHERE clause
        return jpql + " AND e.companyId = :tenantId";
    }
}

// Example repository
@ApplicationScoped
public class PackageJpaRepository extends TenantAwareRepository<Package, PackageId> 
    implements PackageRepository {

    @PersistenceContext
    EntityManager em;

    @Override
    public List<Package> findAll() {
        CompanyId tenantId = requireTenantId();
        return em.createQuery(
            "SELECT p FROM Package p WHERE p.companyId = :tenantId", 
            Package.class
        )
        .setParameter("tenantId", tenantId)
        .getResultList();
    }

    @Override
    public Optional<Package> findById(PackageId id) {
        CompanyId tenantId = requireTenantId();
        return em.createQuery(
            "SELECT p FROM Package p WHERE p.id = :id AND p.companyId = :tenantId",
            Package.class
        )
        .setParameter("id", id)
        .setParameter("tenantId", tenantId)
        .getResultStream()
        .findFirst();
    }
}
```

### Architecture Test (ArchUnit)

```java
@AnalyzeClasses(packages = "com.upkeep")
public class TenantIsolationArchTest {

    @ArchTest
    static final ArchRule repositories_must_extend_tenant_aware = 
        classes()
            .that().resideInAPackage("..persistence..")
            .and().haveSimpleNameEndingWith("Repository")
            .and().areNotInterfaces()
            .should().beAssignableTo(TenantAwareRepository.class)
            .because("All repositories must enforce tenant isolation");

    @ArchTest
    static final ArchRule no_direct_entity_manager_in_resources =
        noClasses()
            .that().resideInAPackage("..rest..")
            .should().dependOnClassesThat().areAssignableTo(EntityManager.class)
            .because("Resources should use use cases, not direct DB access");
}
```

### Integration Test

```java
@QuarkusTest
public class TenantIsolationTest {

    @Test
    void shouldNotAccessOtherCompanyData() {
        // Setup: Create two companies with packages
        String companyA = createCompanyAndGetToken("Company A");
        String companyB = createCompanyAndGetToken("Company B");
        
        String packageIdA = createPackage(companyA, "lodash");
        String packageIdB = createPackage(companyB, "react");

        // User A tries to access User B's package
        given()
            .header("Authorization", "Bearer " + companyA)
        .when()
            .get("/api/companies/{companyBId}/packages/{packageIdB}")
        .then()
            .statusCode(404); // Not 403!
    }

    @Test
    void listPackagesShouldOnlyReturnOwnCompanyPackages() {
        String tokenA = createCompanyAndGetToken("Company A");
        createPackage(tokenA, "lodash");
        createPackage(tokenA, "express");

        String tokenB = createCompanyAndGetToken("Company B");
        createPackage(tokenB, "react");

        // Company A should only see their packages
        given()
            .header("Authorization", "Bearer " + tokenA)
        .when()
            .get("/api/companies/{companyAId}/packages")
        .then()
            .statusCode(200)
            .body("data.size()", is(2))
            .body("data.name", hasItems("lodash", "express"))
            .body("data.name", not(hasItem("react")));
    }
}
```

### NFR Compliance

| Requirement | Implementation |
|-------------|----------------|
| NFR5: Tenant isolation | TenantContext + filtered queries |
| NFR6: Audit trail | AuditEvent includes companyId |
| Security: No enumeration | 404 instead of 403 |

### Dependencies on Previous Stories

- Story 2.1: Company and Membership entities

### E2E Testing Requirements

**Required Playwright tests for this story:**

- [ ] Test: User cannot access resources from another company (404 response)
- [ ] Test: API requests include proper tenant context
- [ ] Test: Cross-tenant URL manipulation returns 404, not 403

**Test file location:** `apps/web/e2e/tenant-isolation.spec.ts`

**Note:** These tests require multiple test users in different companies.

### References

- [Source: architecture.md#Authentication-Security] - Tenant isolation
- [Source: architecture.md#Process-Patterns] - Tenant scoping mandatory
- [Source: epics.md#Story-2.7] - Original acceptance criteria

## Dev Agent Record

### Agent Model Used
_To be filled by dev agent_

### Completion Notes List
_To be filled during implementation_

### Change Log
_To be filled during implementation_

### File List
_To be filled after implementation_

