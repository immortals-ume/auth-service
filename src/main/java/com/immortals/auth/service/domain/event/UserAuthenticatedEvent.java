package com.immortals.auth.service.domain.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Event payload for successful authentication events
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAuthenticatedEvent {
    private String username;
    private Instant authenticatedAt;
    private String ipAddress;
    private String userAgent;
    private boolean rememberMe;
}
