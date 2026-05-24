package com.immortals.auth.service.domain;

import com.immortals.auth.service.audit.AuditingRevisionListener;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.envers.RevisionEntity;

@Entity
@Table(name = "revinfo")
@RevisionEntity(AuditingRevisionListener.class)
@Getter
@Setter
public class AuditingRevisionEntity extends DefaultRevisionEntity {

    private String username;

    private String ipAddress;

    private String userAgent;
}
