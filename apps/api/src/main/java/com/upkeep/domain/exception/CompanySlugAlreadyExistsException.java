package com.upkeep.domain.exception;

public class CompanySlugAlreadyExistsException extends DomainException {
    private final String slug;

    public CompanySlugAlreadyExistsException(String slug) {
        super("This URL is already taken: " + slug);
        this.slug = slug;
    }

    public String getSlug() {
        return slug;
    }
}
