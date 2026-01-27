package com.upkeep.domain.exception;

public class CompanyNotFoundException extends DomainException {
    private final String companyId;

    public CompanyNotFoundException(String companyId) {
        super("Company not found: " + companyId);
        this.companyId = companyId;
    }

    public String getCompanyId() {
        return companyId;
    }
}
