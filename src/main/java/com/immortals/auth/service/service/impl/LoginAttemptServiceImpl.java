package com.immortals.auth.service.service.impl;

import com.immortals.auth.service.domain.entity.LoginAttempt;
import com.immortals.auth.service.repository.LoginAttemptRepository;
import com.immortals.auth.service.service.LoginAttemptService;
import com.immortals.logging.api.ILogger;
import com.immortals.logging.core.LogManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
public class LoginAttemptServiceImpl implements LoginAttemptService {

    private static final ILogger log = LogManager.getInstance().getLogger(LoginAttemptServiceImpl.class);

    private final LoginAttemptRepository repository;

    @Value("${auth.max-login-attempts}")
    private int maxAttempts;

    @Value("${auth.login-block-duration-minutes}")
    private int blockDurationMinutes;

    public LoginAttemptServiceImpl(LoginAttemptRepository repository) {
        this.repository = repository;
    }

    @Override
    public void loginSucceeded(String username) {
        try {
            resetAttempts(username);
            log.debug("Login succeeded for user: {}", username);
        } catch (Exception e) {
            log.error("Error clearing login attempts for user: {}", username, e);
        }
    }

    @Override
    public void loginFailed(String username) {
        try {
            LoginAttempt entity = repository.findByUsername(username)
                    .orElseGet(() -> createNew(username));

            entity.setFailedAttempts(entity.getFailedAttempts() + 1);
            entity.setLastAttemptAt(Instant.now());

            if (entity.getFailedAttempts() >= maxAttempts) {
                entity.setBlockedAt(Instant.now());
                log.warn("User {} blocked after {} attempts", username, entity.getFailedAttempts());
            }

            repository.save(entity);

            log.warn("Login failed for user: {}, attempts: {}", username, entity.getFailedAttempts());

        } catch (Exception e) {
            log.error("Error tracking login failure for user: {}", username, e);
        }
    }

    @Override
    public boolean isBlocked(String username) {
        try {
            LoginAttempt entity = repository.findByUsername(username).orElse(null);

            if (entity == null || entity.getBlockedAt() == null) {
                return false;
            }

            Instant unblockTime = entity.getBlockedAt()
                    .plus(Duration.ofMinutes(blockDurationMinutes));

            if (Instant.now().isBefore(unblockTime)) {
                return true;
            } else {
                unblockUser(username);
                return false;
            }

        } catch (Exception e) {
            log.error("Error checking block for helpers: {}", username, e);
            return false;
        }
    }

    @Override
    public void resetAttempts(String username) {
        try {
            repository.deleteByUsername(username);
            log.debug("Reset login attempts for user: {}", username);
        } catch (Exception e) {
            log.error("Error resetting login attempts for user: {}", username, e);
        }
    }

    @Override
    public int getFailedAttempts(String username) {
        try {
            return Math.toIntExact(repository.findByUsername(username)
                    .map(LoginAttempt::getFailedAttempts)
                    .orElse(0L));
        } catch (Exception e) {
            log.error("Error getting failed attempts for user: {}", username, e);
            return 0;
        }
    }

    private LoginAttempt createNew(String username) {
        return LoginAttempt.builder()
                .username(username)
                .failedAttempts(0L)
                .blockedAt(null)
                .lastAttemptAt(Instant.now())
                .build();
    }

    private void unblockUser(String username) {
        try {
            repository.deleteByUsername(username);
            log.info("User {} unblocked", username);
        } catch (Exception e) {
            log.error("Error unblocking user: {}", username, e);
        }
    }

    public Duration getRemainingBlockTime(String username) {
        try {
            LoginAttempt entity = repository.findByUsername(username).orElse(null);

            if (entity == null || entity.getBlockedAt() == null) {
                return Duration.ZERO;
            }

            Instant unblockTime = entity.getBlockedAt()
                    .plus(Duration.ofMinutes(blockDurationMinutes));

            Instant now = Instant.now();

            if (now.isBefore(unblockTime)) {
                return Duration.between(now, unblockTime);
            }

            return Duration.ZERO;

        } catch (Exception e) {
            log.error("Error getting remaining block time for helpers: {}", username, e);
            return Duration.ZERO;
        }
    }
}
