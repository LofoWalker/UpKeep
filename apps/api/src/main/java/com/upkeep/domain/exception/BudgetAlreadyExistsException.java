package com.upkeep.domain.exception;

/**
 * Thrown when attempting to set a monthly budget for a company that already has one for the current month.
 */
public class BudgetAlreadyExistsException extends DomainException {

    private final String companyId;

    public BudgetAlreadyExistsException(String companyId) {
        super("A budget already exists for this company for the current month");
        this.companyId = companyId;
    }

    public String getCompanyId() {
        return companyId;
    }
}
