package com.immortals.auth.service.event;

import java.time.Instant;

public record OtpRequestedEvent(
        String eventId,
        Instant occurredAt,
        String phoneNumber,
        String otp,
        int ttlSeconds
) {}

