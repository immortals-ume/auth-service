package com.immortals.auth.service.event;

import java.time.Instant;

public record EmailVerificationRequestedEvent(
        String eventId,
        Instant occurredAt,
        String email,
        String verificationLink
) {}

