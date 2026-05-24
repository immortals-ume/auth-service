package com.immortals.auth.service.domain.entity;

import com.immortals.auth.service.audit.Auditable;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.envers.Audited;

import java.time.Instant;

@Entity
@Table(name = "login_attempts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Audited
public class LoginAttempt extends Auditable {

    private String username;

    private Long failedAttempts;

    private Instant blockedAt;

    private Instant updatedAt;
}