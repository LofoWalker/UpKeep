# Copilot Processing - SetCompanyBudgetUseCase Fix

## User Request
The SetCompanyBudgetUseCaseImpl always creates a new budget without checking if one already exists for the company in the current month. This will cause a database constraint violation (uk_budget_company_effective) if called twice in the same month via direct API access. While the UI prevents this by checking budget.exists before showing the form, the API should handle this scenario gracefully. Consider either: 1) Checking if a budget exists and throwing a descriptive domain exception, or 2) Updating the existing budget if one exists for the current month. Based on the story acceptance criteria AC1 "no budget is set", option 1 seems more appropriate for Story 3.1, with updates handled in Story 3.2.

## Analysis
- Current implementation: Always creates new Budget via `Budget.create()`
- Problem: Database constraint violation on uk_budget_company_effective when budget already exists for current month
- Solution needed: Check for existing budget and throw descriptive domain exception
- Story context: 3.1 (set budget) vs 3.2 (update budget) - suggests option 1 is correct

## Action Plan

### Phase 1: Analyze Domain Structure
- [ ] TODO: Examine Budget domain model to understand effective date handling
- [ ] TODO: Check BudgetRepository interface for existing budget lookup methods
- [ ] TODO: Review domain exceptions structure

### Phase 2: Create Domain Exception
- [ ] TODO: Create BudgetAlreadyExistsException following project patterns
- [ ] TODO: Ensure proper package structure (domain/exception/)

### Phase 3: Update Repository Interface
- [ ] TODO: Add method to BudgetRepository to check for existing budget in current month
- [ ] TODO: Follow naming conventions and return Optional<Budget>

### Phase 4: Update Use Case Implementation
- [ ] TODO: Add check for existing budget before creation
- [ ] TODO: Throw BudgetAlreadyExistsException if budget exists
- [ ] TODO: Maintain transaction integrity and audit logging

### Phase 5: Validation and Testing
- [ ] TODO: Check for compilation errors
- [ ] TODO: Verify imports follow explicit import rules
- [ ] TODO: Ensure SOLID principles and clean architecture
