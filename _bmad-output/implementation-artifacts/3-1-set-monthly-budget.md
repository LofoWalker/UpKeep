# Story 3.1: Set Monthly Budget

Status: ready-for-dev

## Story

As a **company Owner**,
I want to set my company's monthly open-source budget,
so that we can start allocating funds to packages.

## Acceptance Criteria

1. **Given** I am an Owner and no budget is set  
   **When** I go to the Budget section  
   **Then** I see an empty state prompting me to set a budget

2. **Given** I am setting the budget  
   **When** I enter an amount (e.g., €500) and currency (EUR, USD)  
   **And** I confirm  
   **Then** the monthly budget is saved  
   **And** an audit event is recorded (FR37)  
   **And** I see the budget displayed with BudgetBar component

3. **Given** a budget exists  
   **When** I view the Budget section  
   **Then** I see: total budget, allocated amount, remaining amount

## Tasks / Subtasks

- [ ] Task 1: Create Budget domain model (AC: #2)
  - [ ] 1.1: Create `Budget` entity
  - [ ] 1.2: Create `Money` value object (amountCents + currency)
  - [ ] 1.3: Create `AuditEvent` entity

- [ ] Task 2: Create budget use case (AC: #2, #3)
  - [ ] 2.1: Create `SetCompanyBudgetUseCase` port
  - [ ] 2.2: Implement with audit event creation
  - [ ] 2.3: Create `GetBudgetSummaryUseCase`

- [ ] Task 3: Create infrastructure (AC: #2)
  - [ ] 3.1: Create database migrations
  - [ ] 3.2: Implement repositories
  - [ ] 3.3: Create REST endpoints

- [ ] Task 4: Create frontend (AC: #1, #2, #3)
  - [ ] 4.1: Create BudgetBar component
  - [ ] 4.2: Create budget setup form
  - [ ] 4.3: Create budget summary view

## Dev Notes

### Domain Model

```java
// Budget Entity
package com.upkeep.domain.model.budget;

public class Budget {
    private final BudgetId id;
    private final CompanyId companyId;
    private Money amount;
    private final Instant effectiveFrom;
    private final Instant createdAt;
    private Instant updatedAt;

    public static Budget create(CompanyId companyId, Money amount) {
        return new Budget(
            BudgetId.generate(),
            companyId,
            amount,
            YearMonth.now().atDay(1).atStartOfDay(ZoneOffset.UTC).toInstant(),
            Instant.now(),
            Instant.now()
        );
    }
}

// Money Value Object
public record Money(long amountCents, Currency currency) {
    public Money {
        if (amountCents < 0) {
            throw new ValidationException("Amount cannot be negative");
        }
    }

    public static Money of(BigDecimal amount, Currency currency) {
        return new Money(
            amount.multiply(BigDecimal.valueOf(100)).longValue(),
            currency
        );
    }

    public BigDecimal toDecimal() {
        return BigDecimal.valueOf(amountCents).divide(BigDecimal.valueOf(100));
    }
}

public enum Currency {
    EUR, USD, GBP
}

// AuditEvent Entity
public class AuditEvent {
    private final AuditEventId id;
    private final CompanyId companyId;
    private final AuditEventType eventType;
    private final UserId actorId;
    private final String targetType;
    private final String targetId;
    private final Map<String, Object> payload;
    private final Instant timestamp;

    public static AuditEvent budgetCreated(CompanyId companyId, UserId actorId, Budget budget) {
        return new AuditEvent(
            AuditEventId.generate(),
            companyId,
            AuditEventType.BUDGET_CREATED,
            actorId,
            "Budget",
            budget.getId().toString(),
            Map.of("amount", budget.getAmount().amountCents(), "currency", budget.getAmount().currency()),
            Instant.now()
        );
    }
}

public enum AuditEventType {
    BUDGET_CREATED,
    BUDGET_UPDATED,
    ALLOCATION_FINALIZED,
    PAYOUT_RUN_EXECUTED,
    CLAIM_VERIFIED
}
```

### Database Schema

```sql
-- V7__create_budgets_table.sql
CREATE TABLE budgets (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    amount_cents BIGINT NOT NULL,
    currency VARCHAR(3) NOT NULL,
    effective_from TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    UNIQUE(company_id, effective_from)
);

CREATE INDEX idx_budgets__company_id ON budgets(company_id);

-- V8__create_audit_events_table.sql
CREATE TABLE audit_events (
    id UUID PRIMARY KEY,
    company_id UUID REFERENCES companies(id) ON DELETE SET NULL,
    event_type VARCHAR(50) NOT NULL,
    actor_id UUID REFERENCES users(id) ON DELETE SET NULL,
    target_type VARCHAR(50),
    target_id VARCHAR(255),
    payload JSONB,
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_audit_events__company_id ON audit_events(company_id);
CREATE INDEX idx_audit_events__event_type ON audit_events(event_type);
CREATE INDEX idx_audit_events__timestamp ON audit_events(timestamp);
```

### Use Case

```java
@ApplicationScoped
public class SetCompanyBudgetUseCaseImpl implements SetCompanyBudgetUseCase {

    @Override
    @Transactional
    public BudgetResult execute(SetBudgetCommand command) {
        CompanyId companyId = CompanyId.from(command.companyId());
        UserId actorId = UserId.from(command.actorUserId());

        // Verify Owner role
        verifyOwnerRole(actorId, companyId);

        Money amount = new Money(command.amountCents(), command.currency());
        Budget budget = Budget.create(companyId, amount);
        budgetRepository.save(budget);

        // Create audit event
        AuditEvent event = AuditEvent.budgetCreated(companyId, actorId, budget);
        auditEventRepository.save(event);

        return new BudgetResult(
            budget.getId().toString(),
            amount.amountCents(),
            amount.currency().name()
        );
    }
}
```

### REST Endpoint

```java
@Path("/api/companies/{companyId}/budget")
@Authenticated
public class BudgetResource {

    @GET
    public Response getBudget(@PathParam("companyId") String companyId) {
        BudgetSummary summary = getBudgetSummaryUseCase.execute(companyId);
        return Response.ok(ApiResponse.success(summary)).build();
    }

    @POST
    public Response setBudget(
        @PathParam("companyId") String companyId,
        @Valid SetBudgetRequest request,
        @Context SecurityContext ctx
    ) {
        BudgetResult result = setBudgetUseCase.execute(new SetBudgetCommand(
            companyId,
            ctx.getUserPrincipal().getName(),
            request.amountCents(),
            Currency.valueOf(request.currency())
        ));
        return Response.status(201).entity(ApiResponse.success(result)).build();
    }
}
```

### BudgetBar Component

```tsx
// apps/web/src/components/common/BudgetBar.tsx
interface BudgetBarProps {
  totalCents: number
  allocatedCents: number
  currency: string
}

export function BudgetBar({ totalCents, allocatedCents, currency }: BudgetBarProps) {
  const remainingCents = totalCents - allocatedCents
  const percentage = totalCents > 0 ? (allocatedCents / totalCents) * 100 : 0

  return (
    <div className="space-y-2">
      <div className="flex justify-between text-sm">
        <span className="text-muted-foreground">Budget Usage</span>
        <span className="font-medium">
          {formatCurrency(allocatedCents, currency)} / {formatCurrency(totalCents, currency)}
        </span>
      </div>
      
      <div className="h-3 bg-muted rounded-full overflow-hidden">
        <div 
          className={cn(
            "h-full rounded-full transition-all",
            percentage > 90 ? "bg-warning" : "bg-primary"
          )}
          style={{ width: `${Math.min(percentage, 100)}%` }}
        />
      </div>

      <div className="flex justify-between text-xs text-muted-foreground">
        <span>{percentage.toFixed(0)}% allocated</span>
        <span>{formatCurrency(remainingCents, currency)} remaining</span>
      </div>
    </div>
  )
}
```

### Budget Setup Form

```tsx
export function BudgetSetupForm() {
  const { currentCompany } = useCompanyContext()
  const queryClient = useQueryClient()

  const { mutate, isPending } = useMutation({
    mutationFn: (data: BudgetData) => 
      api.post(`/companies/${currentCompany.id}/budget`, {
        amountCents: Math.round(data.amount * 100),
        currency: data.currency,
      }),
    onSuccess: () => {
      queryClient.invalidateQueries(['budget'])
      toast.success('Budget set successfully!')
    },
  })

  return (
    <form onSubmit={handleSubmit((data) => mutate(data))}>
      <div className="grid gap-4 sm:grid-cols-2">
        <div>
          <Label>Monthly Budget</Label>
          <Input
            type="number"
            step="0.01"
            min="0"
            {...register('amount', { required: true, min: 0 })}
          />
        </div>
        <div>
          <Label>Currency</Label>
          <Select {...register('currency')}>
            <SelectTrigger>
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="EUR">EUR (€)</SelectItem>
              <SelectItem value="USD">USD ($)</SelectItem>
              <SelectItem value="GBP">GBP (£)</SelectItem>
            </SelectContent>
          </Select>
        </div>
      </div>
      <Button type="submit" disabled={isPending} className="mt-4">
        {isPending ? 'Saving...' : 'Set Budget'}
      </Button>
    </form>
  )
}
```

### Dependencies on Previous Stories

- Story 2.1: Company entity
- Story 2.7: Tenant isolation

### References

- [Source: architecture.md#Data-Architecture] - Money in cents
- [Source: epics.md#Story-3.1] - Original acceptance criteria
- FR37: Audit event for budget changes

## Dev Agent Record

### Agent Model Used
_To be filled by dev agent_

### Completion Notes List
_To be filled during implementation_

### Change Log
_To be filled during implementation_

### File List
_To be filled after implementation_

