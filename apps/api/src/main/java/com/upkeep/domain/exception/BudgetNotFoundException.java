package com.upkeep.domain.exception;

/**
 * Thrown when attempting to update a budget that does not exist.
 */
public class BudgetNotFoundException extends DomainException {

    private final String companyId;

    public BudgetNotFoundException(String companyId) {
        super("No budget found for this company");
        this.companyId = companyId;
    }

    public String getCompanyId() {
        return companyId;
    }
}

