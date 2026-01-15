package com.upkeep.domain.model;

/**
 * Base abstract class for all domain entities.
 * Entities have identity and business logic.
 */
public abstract class Entity {
    protected Long id;

    public Entity() {
    }

    public Entity(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}

