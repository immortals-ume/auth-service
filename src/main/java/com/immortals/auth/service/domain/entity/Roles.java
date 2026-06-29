package com.immortals.auth.service.domain.entity;

import com.immortals.auth.service.domain.Auditable;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity
@SequenceGenerator(name = "id_seq", sequenceName = "role_seq", schema = "user_auth", allocationSize = 50)
@Table(
        name = "role",
        schema = "user_auth",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_role_name", columnNames = "role_name")
        },
        indexes = {
                @Index(name = "idx_role_name", columnList = "role_name")
        }
)
@Audited
public class Roles extends Auditable implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Column(name = "role_id", nullable = false, unique = true, updatable = false)
    private Long roleId;

    @Column(name = "role_name", nullable = false, length = 20)
    @NotNull
    private String roleName;

    @Column(name = "description", length = 200)
    private String description;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "role_permission",
            schema = "user_auth",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<Permissions> permissions = new HashSet<>();

    @NotAudited
    @ManyToMany(mappedBy = "roles")
    private Set<User> users = new HashSet<>();

    @Column(name = "active_ind", nullable = false)
    @NotNull
    private Boolean activeInd;

    public static Builder builder() {
        return new Builder();
    }

    @PrePersist
    public void generateRoleId() {
        if (this.roleId == null) {
            this.roleId = this.getId();
        }
    }

    public Long getRoleId() {
        return roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    public String getDescription() {
        return description;
    }

    public Boolean getActiveInd() {
        return activeInd;
    }

    public Set<Permissions> getPermissions() {
        return permissions;
    }

    public Set<User> getUsers() {
        return users;
    }

    public static final class Builder extends Auditable.Builder<Roles, Builder> {

        private String roleName;
        private String description;
        private Boolean activeInd;
        private Set<Permissions> permissions = new HashSet<>();
        private Set<User> users = new HashSet<>();

        private Builder() {
        }

        public Builder roleName(String roleName) {
            if (roleName == null || roleName.isBlank())
                throw new IllegalArgumentException("roleName must not be blank");
            if (roleName.length() > 20)
                throw new IllegalArgumentException("roleName must not exceed 20 characters");
            this.roleName = roleName;
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

        public Builder permissions(Set<Permissions> permissions) {
            this.permissions = permissions != null ? permissions : new HashSet<>();
            return self();
        }

        public Builder users(Set<User> users) {
            this.users = users != null ? users : new HashSet<>();
            return self();
        }

        @Override
        public Roles build() {
            if (roleName == null || roleName.isBlank())
                throw new IllegalStateException("roleName is required");
            if (activeInd == null)
                throw new IllegalStateException("activeInd is required");

            Roles role = new Roles();
            applyAuditFields(role);

            role.roleName = this.roleName;
            role.description = this.description;
            role.activeInd = this.activeInd;
            role.permissions = new HashSet<>(this.permissions);
            role.users = new HashSet<>(this.users);

            return role;
        }
    }
}

