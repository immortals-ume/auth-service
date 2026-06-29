package com.immortals.auth.service.domain.entity;

import com.immortals.auth.service.domain.Auditable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.envers.Audited;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

@Entity
@SequenceGenerator(name = "id_seq", sequenceName = "login_attempt_seq", schema = "user_auth", allocationSize = 50)
@Table(
        name = "login_attempt",
        schema = "user_auth",
        indexes = {
                @Index(name = "idx_login_attempt_username", columnList = "username"),
                @Index(name = "idx_login_attempt_ip_address", columnList = "ip_address"),
                @Index(name = "idx_login_attempt_blocked", columnList = "account_blocked"),
                @Index(name = "idx_login_attempt_last_attempt", columnList = "last_attempt_at")
        }
)
@Audited
public class LoginAttempt extends Auditable implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Column(name = "username", nullable = false, length = 100)
    @NotBlank
    @Size(max = 100)
    private String username;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "failed_attempts", nullable = false)
    @NotNull
    private Long failedAttempts = 0L;

    @Column(name = "account_blocked", nullable = false)
    @NotNull
    private Boolean accountBlocked = false;

    @Column(name = "blocked_at")
    private Instant blockedAt;

    @Column(name = "block_expires_at")
    private Instant blockExpiresAt;

    @Column(name = "last_attempt_at", nullable = false)
    private Instant lastAttemptAt;

    @Column(name = "last_success_at")
    private Instant lastSuccessAt;

    @Column(name = "active_ind", nullable = false)
    private Boolean activeInd = true;

    public static Builder builder() {
        return new Builder();
    }

    public String getUsername() {
        return username;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public Long getFailedAttempts() {
        return failedAttempts;
    }

    public void setFailedAttempts(Long failedAttempts) {
        this.failedAttempts = failedAttempts;
    }

    public Boolean getAccountBlocked() {
        return accountBlocked;
    }

    public Instant getBlockedAt() {
        return blockedAt;
    }

    public void setBlockedAt(Instant blockedAt) {
        this.blockedAt = blockedAt;
    }

    public Instant getBlockExpiresAt() {
        return blockExpiresAt;
    }

    public Instant getLastAttemptAt() {
        return lastAttemptAt;
    }

    public void setLastAttemptAt(Instant lastAttemptAt) {
        this.lastAttemptAt = lastAttemptAt;
    }

    public Instant getLastSuccessAt() {
        return lastSuccessAt;
    }

    public Boolean getActiveInd() {
        return activeInd;
    }

    public static final class Builder extends Auditable.Builder<LoginAttempt, Builder> {

        private String username;
        private String ipAddress;
        private String userAgent;
        private Long failedAttempts = 0L;
        private Boolean accountBlocked = false;
        private Instant blockedAt;
        private Instant blockExpiresAt;
        private Instant lastAttemptAt;
        private Instant lastSuccessAt;
        private Boolean activeInd = true;

        private Builder() {
        }

        public Builder username(String username) {
            this.username = username;
            return self();
        }

        public Builder ipAddress(String ipAddress) {
            this.ipAddress = ipAddress;
            return self();
        }

        public Builder userAgent(String userAgent) {
            this.userAgent = userAgent;
            return self();
        }

        public Builder failedAttempts(Long failedAttempts) {
            this.failedAttempts = failedAttempts;
            return self();
        }

        public Builder accountBlocked(Boolean accountBlocked) {
            this.accountBlocked = accountBlocked;
            return self();
        }

        public Builder blockedAt(Instant blockedAt) {
            this.blockedAt = blockedAt;
            return self();
        }

        public Builder blockExpiresAt(Instant blockExpiresAt) {
            this.blockExpiresAt = blockExpiresAt;
            return self();
        }

        public Builder lastAttemptAt(Instant lastAttemptAt) {
            this.lastAttemptAt = lastAttemptAt;
            return self();
        }

        public Builder lastSuccessAt(Instant lastSuccessAt) {
            this.lastSuccessAt = lastSuccessAt;
            return self();
        }

        public Builder activeInd(Boolean activeInd) {
            this.activeInd = activeInd;
            return self();
        }

        @Override
        public LoginAttempt build() {

            if (username == null || username.isBlank()) {
                throw new IllegalStateException("username is required");
            }

            LoginAttempt entity = new LoginAttempt();

            applyAuditFields(entity);

            entity.username = this.username;
            entity.ipAddress = this.ipAddress;
            entity.userAgent = this.userAgent;
            entity.failedAttempts = this.failedAttempts;
            entity.accountBlocked = this.accountBlocked;
            entity.blockedAt = this.blockedAt;
            entity.blockExpiresAt = this.blockExpiresAt;
            entity.lastAttemptAt = this.lastAttemptAt;
            entity.lastSuccessAt = this.lastSuccessAt;
            entity.activeInd = this.activeInd;

            return entity;
        }
    }
}
