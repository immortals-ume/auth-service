package com.immortals.auth.service.service.impl;

import com.immortals.auth.service.domain.entity.BlacklistedToken;
import com.immortals.auth.service.repository.BlacklistedTokenRepository;
import com.immortals.auth.service.service.TokenBlacklistService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
public class TokenBlacklistServiceImpl implements TokenBlacklistService {

    private final BlacklistedTokenRepository repository;

    public TokenBlacklistServiceImpl(BlacklistedTokenRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public void blacklist(String jti, long expiresAtEpochSecond) {
        repository.save(new BlacklistedToken(jti, Instant.ofEpochSecond(expiresAtEpochSecond)));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isBlacklisted(String jti) {
        return repository.findById(jti)
                .map(t -> Instant.now().isBefore(t.getExpiresAt()))
                .orElse(false);
    }

    @Scheduled(fixedDelay = 300_000)
    @Transactional
    public void evictExpired() {
        repository.deleteByExpiresAtBefore(Instant.now());
    }
}

