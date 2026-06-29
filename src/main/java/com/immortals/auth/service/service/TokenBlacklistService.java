package com.immortals.auth.service.service;

public interface TokenBlacklistService {

    void blacklist(String jti, long expiresAtEpochSecond);

    boolean isBlacklisted(String jti);
}
