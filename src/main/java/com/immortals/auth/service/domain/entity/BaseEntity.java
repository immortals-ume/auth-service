package com.immortals.auth.service.domain.entity;

import jakarta.persistence.*;

@MappedSuperclass
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "id_seq")
    private Long id;

    @Version
    private Long version;

    public Long getId() {
        return id;
    }

    public Long getVersion() {
        return version;
    }

    protected static abstract class Builder<T extends BaseEntity, B extends Builder<T, B>> {

        @SuppressWarnings("unchecked")
        protected B self() {
            return (B) this;
        }

        public abstract T build();
    }
}
