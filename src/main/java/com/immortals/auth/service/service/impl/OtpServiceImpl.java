package com.immortals.auth.service.service.impl;

import com.immortals.auth.service.service.OtpService;
import com.immortals.logging.api.ILogger;
import com.immortals.logging.core.LogManager;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class OtpServiceImpl implements OtpService {

    private static final ILogger log = LogManager.getInstance().getLogger(OtpServiceImpl.class);

    private static final int OTP_TTL_SECONDS = 300;
    private static final SecureRandom RANDOM = new SecureRandom();

    private record OtpEntry(String otp, Instant expiresAt) {
        boolean isExpired() { return Instant.now().isAfter(expiresAt); }
    }

    private final Map<String, OtpEntry> store = new ConcurrentHashMap<>();

    @Override
    public void generateAndSend(String phoneNumber) {
        String otp = String.format("%06d", RANDOM.nextInt(1_000_000));
        store.put(phoneNumber, new OtpEntry(otp, Instant.now().plusSeconds(OTP_TTL_SECONDS)));
        // SMS dispatch not yet integrated — log OTP for dev purposes only
        log.info("OTP generated for phone {}. SMS dispatch not configured — OTP: {}", phoneNumber, otp);
    }

    @Override
    public boolean verify(String phoneNumber, String otp) {
        OtpEntry entry = store.get(phoneNumber);
        if (entry == null || entry.isExpired()) {
            store.remove(phoneNumber);
            return false;
        }
        if (entry.otp().equals(otp)) {
            store.remove(phoneNumber);
            return true;
        }
        return false;
    }

    @Override
    public void invalidate(String phoneNumber) {
        store.remove(phoneNumber);
    }
}
