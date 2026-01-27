package com.upkeep.domain.exception;

public class MembershipNotFoundException extends DomainException {
    private final String customerId;
    private final String companyId;

    public MembershipNotFoundException(String customerId, String companyId) {
        super("User is not a member of this company");
        this.customerId = customerId;
        this.companyId = companyId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getCompanyId() {
        return companyId;
    }
}
