package com.immortals.auth.service.audit;

import com.immortals.auth.service.domain.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.Instant;

@Getter
@Setter
@MappedSuperclass
@SuperBuilder
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@AllArgsConstructor
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
}