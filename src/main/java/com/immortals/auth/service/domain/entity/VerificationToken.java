package com.immortals.auth.service.domain.entity;

import com.immortals.auth.service.domain.Auditable;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@SequenceGenerator(name = "id_seq", sequenceName = "verification_token_seq", schema = "user_auth", allocationSize = 50)
@Table(
        name = "verification_tokens",
        schema = "user_auth",
        indexes = {
                @Index(name = "idx_vt_token", columnList = "token"),
                @Index(name = "idx_vt_user_type", columnList = "user_id, type")
        }
)
public class VerificationToken extends Auditable {

    public enum TokenType {
        EMAIL_VERIFICATION,
        PASSWORD_RESET
    }

    @Column(nullable = false, unique = true, length = 36)
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TokenType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "used", nullable = false)
    private boolean used = false;

    public VerificationToken() {}

    public String token() { return token; }
    public TokenType type() { return type; }
    public User user() { return user; }
    public Instant expiresAt() { return expiresAt; }
    public boolean used() { return used; }

    public void markUsed() { this.used = true; }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public static Builder builder() { return new Builder(); }

    public static final class Builder extends Auditable.Builder<VerificationToken, Builder> {
        private String token;
        private TokenType type;
        private User user;
        private Instant expiresAt;
        private boolean used = false;

        private Builder() {}

        public Builder token(String token) { this.token = token; return self(); }
        public Builder type(TokenType type) { this.type = type; return self(); }
        public Builder user(User user) { this.user = user; return self(); }
        public Builder expiresAt(Instant expiresAt) { this.expiresAt = expiresAt; return self(); }
        public Builder used(boolean used) { this.used = used; return self(); }

        @Override
        public VerificationToken build() {
            VerificationToken vt = new VerificationToken();
            applyAuditFields(vt);
            vt.token = this.token;
            vt.type = this.type;
            vt.user = this.user;
            vt.expiresAt = this.expiresAt;
            vt.used = this.used;
            return vt;
        }
    }
}
