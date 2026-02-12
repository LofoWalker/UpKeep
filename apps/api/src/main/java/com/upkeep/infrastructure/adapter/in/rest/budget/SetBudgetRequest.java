package com.upkeep.infrastructure.adapter.in.rest.budget;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
public record SetBudgetRequest(
        @Min(value = 100, message = "Budget must be at least 100 cents (1.00)")
        long amountCents,
        @NotBlank(message = "Currency is required")
        @Pattern(regexp = "EUR|USD|GBP", message = "Currency must be EUR, USD, or GBP")
        String currency
) {
}
