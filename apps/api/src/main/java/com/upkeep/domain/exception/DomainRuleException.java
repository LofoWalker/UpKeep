package com.upkeep.domain.exception;
public class DomainRuleException extends ApiException {
    public DomainRuleException(String message) {
        super("DOMAIN_RULE_VIOLATION", message, 422);
    }
}
