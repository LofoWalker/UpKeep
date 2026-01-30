package com.upkeep.infrastructure.adapter.out.persistence.budget;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "budgets")
public class BudgetEntity extends PanacheEntityBase {

    @Id
    @Column(name = "id", nullable = false)
    public UUID id;

    @Column(name = "company_id", nullable = false)
    public UUID companyId;

    @Column(name = "amount_cents", nullable = false)
    public Long amountCents;

    @Column(name = "currency", nullable = false, length = 3)
    public String currency;

    @Column(name = "effective_from", nullable = false)
    public Instant effectiveFrom;

    @Column(name = "created_at", nullable = false)
    public Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    public Instant updatedAt;
}
