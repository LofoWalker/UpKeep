-- Budgets table for company monthly open-source budget
CREATE TABLE budgets (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    amount_cents BIGINT NOT NULL,
    currency VARCHAR(3) NOT NULL,
    effective_from TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_budget_company_effective UNIQUE (company_id, effective_from)
);

CREATE INDEX idx_budgets_company_id ON budgets(company_id);
CREATE INDEX idx_budgets_effective_from ON budgets(effective_from);
