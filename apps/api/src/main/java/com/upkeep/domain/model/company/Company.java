package com.upkeep.domain.model.company;

import java.time.Instant;

public class Company {
    private final CompanyId id;
    private final CompanyName name;
    private final CompanySlug slug;
    private final Instant createdAt;
    private Instant updatedAt;

    private Company(CompanyId id,
                    CompanyName name,
                    CompanySlug slug,
                    Instant createdAt,
                    Instant updatedAt) {
        this.id = id;
        this.name = name;
        this.slug = slug;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Company create(CompanyName name, CompanySlug slug) {
        Instant now = Instant.now();
        return new Company(
                CompanyId.generate(),
                name,
                slug,
                now,
                now
        );
    }

    public static Company reconstitute(CompanyId id,
                                       CompanyName name,
                                       CompanySlug slug,
                                       Instant createdAt,
                                       Instant updatedAt) {
        return new Company(id, name, slug, createdAt, updatedAt);
    }

    public void updateTimestamp() {
        this.updatedAt = Instant.now();
    }

    public CompanyId getId() {
        return id;
    }

    public CompanyName getName() {
        return name;
    }

    public CompanySlug getSlug() {
        return slug;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
