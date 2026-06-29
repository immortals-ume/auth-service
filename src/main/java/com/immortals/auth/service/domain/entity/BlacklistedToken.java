package com.immortals.auth.service.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "blacklisted_tokens", schema = "user_auth")
public class BlacklistedToken {

    @Id
    @Column(name = "jti", length = 36)
    private String jti;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    public BlacklistedToken() {}

    public BlacklistedToken(String jti, Instant expiresAt) {
        this.jti = jti;
        this.expiresAt = expiresAt;
    }

    public String getJti() { return jti; }
    public Instant getExpiresAt() { return expiresAt; }
}

