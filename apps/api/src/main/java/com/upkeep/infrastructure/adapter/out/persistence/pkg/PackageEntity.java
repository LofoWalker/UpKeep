package com.upkeep.infrastructure.adapter.out.persistence.pkg;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "packages")
public class PackageEntity extends PanacheEntityBase {

    @Id
    @Column(name = "id", nullable = false)
    public UUID id;

    @Column(name = "company_id", nullable = false)
    public UUID companyId;

    @Column(name = "name", nullable = false)
    public String name;

    @Column(name = "registry", nullable = false, length = 50)
    public String registry;

    @Column(name = "imported_at", nullable = false)
    public Instant importedAt;
}

