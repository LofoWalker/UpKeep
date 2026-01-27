package com.upkeep.infrastructure.adapter.out.persistence.membership;

import com.upkeep.domain.model.membership.Role;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "memberships")
public class MembershipEntity extends PanacheEntityBase {

    @Id
    @Column(name = "id", nullable = false)
    public UUID id;

    @Column(name = "customer_id", nullable = false)
    public UUID customerId;

    @Column(name = "company_id", nullable = false)
    public UUID companyId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    public Role role;

    @Column(name = "joined_at", nullable = false)
    public Instant joinedAt;

    @Column(name = "updated_at", nullable = false)
    public Instant updatedAt;
}
