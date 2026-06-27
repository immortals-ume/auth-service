package com.immortals.auth.service.domain;

import com.immortals.auth.service.domain.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.Instant;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class Auditable extends BaseEntity implements Serializable {

    @CreatedBy
    @Column(name = "created_by", updatable = false, nullable = false)
    protected String createdBy;

    @CreatedDate
    @Column(name = "created_date", updatable = false, nullable = false)
    protected Instant createdDate;

    @LastModifiedBy
    @Column(name = "updated_by")
    protected String updatedBy;

    @LastModifiedDate
    @Column(name = "updated_date")
    protected Instant updatedDate;

    @Column(name = "deleted_by")
    protected String deletedBy;

    @Column(name = "deleted_date")
    protected Instant deletedDate;

    public String getCreatedBy() {
        return createdBy;
    }

    public Instant getCreatedDate() {
        return createdDate;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public Instant getUpdatedDate() {
        return updatedDate;
    }

    public String getDeletedBy() {
        return deletedBy;
    }

    public Instant getDeletedDate() {
        return deletedDate;
    }

    protected static abstract class Builder<T extends Auditable, B extends Builder<T, B>>
            extends BaseEntity.Builder<T, B> {

        private String createdBy;
        private Instant createdDate;
        private String updatedBy;
        private Instant updatedDate;
        private String deletedBy;
        private Instant deletedDate;

        public B createdBy(String createdBy) {
            this.createdBy = createdBy;
            return self();
        }

        public B createdDate(Instant createdDate) {
            this.createdDate = createdDate;
            return self();
        }

        public B updatedBy(String updatedBy) {
            this.updatedBy = updatedBy;
            return self();
        }

        public B updatedDate(Instant updatedDate) {
            this.updatedDate = updatedDate;
            return self();
        }

        public B deletedBy(String deletedBy) {
            this.deletedBy = deletedBy;
            return self();
        }

        public B deletedDate(Instant deletedDate) {
            this.deletedDate = deletedDate;
            return self();
        }

        protected void applyAuditFields(T entity) {
            entity.createdBy = this.createdBy;
            entity.createdDate = this.createdDate;
            entity.updatedBy = this.updatedBy;
            entity.updatedDate = this.updatedDate;
            entity.deletedBy = this.deletedBy;
            entity.deletedDate = this.deletedDate;
        }
    }
}