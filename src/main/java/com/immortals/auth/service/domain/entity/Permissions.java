package com.immortals.auth.service.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.immortals.auth.service.domain.Auditable;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.envers.Audited;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity
@SequenceGenerator(name = "id_seq", sequenceName = "permission_seq", schema = "user_auth", allocationSize = 50)
@Table(
        name = "permission",
        schema = "user_auth",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_permission_name", columnNames = "permission_name")
        },
        indexes = {
                @Index(name = "idx_permission_name", columnList = "permission_name")
        }
)
@Audited
public class Permissions extends Auditable implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Column(name = "permission_name", nullable = false, length = 50)
    @NotNull
    private String name;

    @Column(name = "description", length = 200)
    private String description;

    @Column(name = "active_ind", nullable = false)
    @NotNull
    private Boolean activeInd;

    @ManyToMany(mappedBy = "permissions")
    @JsonIgnore
    private Set<Roles> roles = new HashSet<>();

    public static Builder builder() {
        return new Builder();
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Boolean getActiveInd() {
        return activeInd;
    }

    public Set<Roles> getRoles() {
        return roles;
    }

    public static final class Builder extends Auditable.Builder<Permissions, Builder> {

        private String name;
        private String description;
        private Boolean activeInd = true;

        private Builder() {
        }

        public Builder name(String name) {
            this.name = name;
            return self();
        }

        public Builder description(String description) {
            this.description = description;
            return self();
        }

        public Builder activeInd(Boolean activeInd) {
            this.activeInd = activeInd;
            return self();
        }

        @Override
        public Permissions build() {
            if (name == null || name.isBlank())
                throw new IllegalStateException("name is required");

            Permissions permission = new Permissions();
            applyAuditFields(permission);
            permission.name = this.name;
            permission.description = this.description;
            permission.activeInd = this.activeInd;
            return permission;
        }
    }
}
