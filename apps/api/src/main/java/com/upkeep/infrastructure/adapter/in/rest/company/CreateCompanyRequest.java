package com.upkeep.infrastructure.adapter.in.rest.company;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCompanyRequest(
        @NotBlank(message = "Company name is required")
        @Size(min = 2, max = 100, message = "Company name must be between 2 and 100 characters")
        String name,

        @Size(min = 2, max = 50, message = "Company slug must be between 2 and 50 characters")
        String slug
) {
}
