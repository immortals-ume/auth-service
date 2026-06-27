package com.immortals.auth.service.service.impl;

import com.immortals.auth.service.service.TokenBlacklistService;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Component
public class TokenBlacklistServiceImpl implements TokenBlacklistService {

    private final Map<String, Long> blacklist = new ConcurrentHashMap<>();

    public void blacklist(String jti, long expiresAtEpochSecond) {
        blacklist.put(jti, expiresAtEpochSecond);
        evictExpired();
    }

    public boolean isBlacklisted(String jti) {
        Long expiry = blacklist.get(jti);
        if (expiry == null) return false;
        if (Instant.now().getEpochSecond() > expiry) {
            blacklist.remove(jti);
            return false;
        }
        return true;
    }

    private void evictExpired() {
        long now = Instant.now().getEpochSecond();
        blacklist.entrySet().removeIf(e -> e.getValue() < now);
    }
}