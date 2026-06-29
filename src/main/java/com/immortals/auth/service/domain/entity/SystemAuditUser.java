package com.immortals.auth.service.domain.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "system_audit")
public class SystemAuditUser {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
            generator = "system_audit_seq")
    @SequenceGenerator(name = "system_audit_seq",
            sequenceName = "system_audit_seq",
            allocationSize = 1)
    private Long id;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @Column(name = "description")
    private String description;

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public boolean isActive() {
        return active;
    }

    public String getDescription() {
        return description;
    }
}
