package com.immortals.auth.service.repository;

import com.immortals.auth.service.domain.entity.BlacklistedToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Repository
public interface BlacklistedTokenRepository extends JpaRepository<BlacklistedToken, String> {

    @Transactional
    @Modifying
    void deleteByExpiresAtBefore(Instant cutoff);
}

