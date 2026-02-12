package com.upkeep.infrastructure.adapter.out.persistence.audit;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "audit_events")
public class AuditEventEntity extends PanacheEntityBase {

    @Id
    @Column(name = "id", nullable = false)
    public UUID id;

    @Column(name = "company_id")
    public UUID companyId;

    @Column(name = "event_type", nullable = false, length = 50)
    public String eventType;

    @Column(name = "actor_id")
    public UUID actorId;

    @Column(name = "target_type", length = 50)
    public String targetType;

    @Column(name = "target_id")
    public String targetId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", columnDefinition = "jsonb")
    public String payload;

    @Column(name = "timestamp", nullable = false)
    public Instant timestamp;
}
