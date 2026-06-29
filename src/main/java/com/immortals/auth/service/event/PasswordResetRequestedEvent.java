package com.immortals.auth.service.event;

import java.time.Instant;

public record PasswordResetRequestedEvent(
        String eventId,
        Instant occurredAt,
        String email,
        String resetLink
) {}

