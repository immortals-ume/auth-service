package com.immortals.auth.service.unit.service;

import com.immortals.auth.service.domain.entity.LoginAttempt;
import com.immortals.auth.service.repository.LoginAttemptRepository;
import com.immortals.auth.service.service.impl.LoginAttemptServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginAttemptServiceImplTest {

    @Mock LoginAttemptRepository repository;

    @InjectMocks
    LoginAttemptServiceImpl loginAttemptService;

    @BeforeEach
    void injectProperties() {
        ReflectionTestUtils.setField(loginAttemptService, "maxAttempts", 5);
        ReflectionTestUtils.setField(loginAttemptService, "blockDurationMinutes", 15);
    }

    private LoginAttempt buildAttempt(String username, long failed, Instant blockedAt) {
        return LoginAttempt.builder()
                .username(username)
                .failedAttempts(failed)
                .blockedAt(blockedAt)
                .lastAttemptAt(Instant.now())
                .build();
    }

    @Test
    void isBlocked_whenNoRecord_returnsFalse() {
        when(repository.findByUsername("user")).thenReturn(Optional.empty());
        assertThat(loginAttemptService.isBlocked("user")).isFalse();
    }

    @Test
    void isBlocked_whenBlockedButWindowNotExpired_returnsTrue() {
        LoginAttempt attempt = buildAttempt("user", 5, Instant.now().minusSeconds(60));
        when(repository.findByUsername("user")).thenReturn(Optional.of(attempt));

        assertThat(loginAttemptService.isBlocked("user")).isTrue();
    }

    @Test
    void isBlocked_whenBlockedWindowExpired_returnsFalseAndClearsRecord() {
        LoginAttempt attempt = buildAttempt("user", 5, Instant.now().minusSeconds(3600));
        when(repository.findByUsername("user")).thenReturn(Optional.of(attempt));

        assertThat(loginAttemptService.isBlocked("user")).isFalse();
        verify(repository).deleteByUsername("user");
    }

    @Test
    void loginFailed_incrementsFailedAttempts() {
        LoginAttempt attempt = buildAttempt("user", 2, null);
        when(repository.findByUsername("user")).thenReturn(Optional.of(attempt));

        loginAttemptService.loginFailed("user");

        verify(repository).save(any(LoginAttempt.class));
        assertThat(attempt.getFailedAttempts()).isEqualTo(3);
    }

    @Test
    void loginFailed_whenReachesMaxAttempts_setsBlockedAt() {
        LoginAttempt attempt = buildAttempt("user", 4, null);
        when(repository.findByUsername("user")).thenReturn(Optional.of(attempt));

        loginAttemptService.loginFailed("user");

        assertThat(attempt.getBlockedAt()).isNotNull();
        verify(repository).save(attempt);
    }

    @Test
    void loginFailed_whenNoExistingRecord_createsNew() {
        when(repository.findByUsername("newuser")).thenReturn(Optional.empty());

        loginAttemptService.loginFailed("newuser");

        verify(repository).save(any(LoginAttempt.class));
    }

    @Test
    void loginSucceeded_deletesAttemptRecord() {
        loginAttemptService.loginSucceeded("user");
        verify(repository).deleteByUsername("user");
    }

    @Test
    void getFailedAttempts_returnsCountFromRepository() {
        LoginAttempt attempt = buildAttempt("user", 3, null);
        when(repository.findByUsername("user")).thenReturn(Optional.of(attempt));

        assertThat(loginAttemptService.getFailedAttempts("user")).isEqualTo(3);
    }

    @Test
    void getFailedAttempts_whenNoRecord_returnsZero() {
        when(repository.findByUsername("user")).thenReturn(Optional.empty());
        assertThat(loginAttemptService.getFailedAttempts("user")).isZero();
    }

    @Test
    void getRemainingBlockTime_whenBlockedAndWindowActive_returnsPositiveDuration() {
        LoginAttempt attempt = buildAttempt("user", 5, Instant.now().minusSeconds(60));
        when(repository.findByUsername("user")).thenReturn(Optional.of(attempt));

        assertThat(loginAttemptService.getRemainingBlockTime("user").toSeconds()).isPositive();
    }

    @Test
    void getRemainingBlockTime_whenNotBlocked_returnsZero() {
        when(repository.findByUsername("user")).thenReturn(Optional.empty());
        assertThat(loginAttemptService.getRemainingBlockTime("user").isZero()).isTrue();
    }

    @Test
    void resetAttempts_deletesRecord() {
        loginAttemptService.resetAttempts("user");
        verify(repository).deleteByUsername("user");
    }

    // ──────────────── exception-path coverage (all catch blocks return safely) ────────────────

    @Test
    void loginSucceeded_whenRepositoryThrows_doesNotPropagate() {
        doThrow(new RuntimeException("db error")).when(repository).deleteByUsername("user");
        loginAttemptService.loginSucceeded("user");  // must not throw
    }

    @Test
    void loginFailed_whenRepositoryThrows_doesNotPropagate() {
        when(repository.findByUsername("user")).thenThrow(new RuntimeException("db error"));
        loginAttemptService.loginFailed("user");  // must not throw
    }

    @Test
    void isBlocked_whenRepositoryThrows_returnsFalse() {
        when(repository.findByUsername("user")).thenThrow(new RuntimeException("db error"));
        assertThat(loginAttemptService.isBlocked("user")).isFalse();
    }

    @Test
    void isBlocked_whenRecordExistsWithNullBlockedAt_returnsFalse() {
        LoginAttempt attempt = buildAttempt("user", 2, null);
        when(repository.findByUsername("user")).thenReturn(Optional.of(attempt));
        assertThat(loginAttemptService.isBlocked("user")).isFalse();
    }

    @Test
    void resetAttempts_whenRepositoryThrows_doesNotPropagate() {
        doThrow(new RuntimeException("db error")).when(repository).deleteByUsername("user");
        loginAttemptService.resetAttempts("user");  // must not throw
    }

    @Test
    void getFailedAttempts_whenRepositoryThrows_returnsZero() {
        when(repository.findByUsername("user")).thenThrow(new RuntimeException("db error"));
        assertThat(loginAttemptService.getFailedAttempts("user")).isZero();
    }

    @Test
    void getRemainingBlockTime_whenRepositoryThrows_returnsZero() {
        when(repository.findByUsername("user")).thenThrow(new RuntimeException("db error"));
        assertThat(loginAttemptService.getRemainingBlockTime("user").isZero()).isTrue();
    }

    @Test
    void getRemainingBlockTime_whenBlockExpired_returnsZero() {
        LoginAttempt attempt = buildAttempt("user", 5, Instant.now().minusSeconds(3600));
        when(repository.findByUsername("user")).thenReturn(Optional.of(attempt));
        assertThat(loginAttemptService.getRemainingBlockTime("user").isZero()).isTrue();
    }

    @Test
    void getRemainingBlockTime_whenNoBlockedAt_returnsZero() {
        LoginAttempt attempt = buildAttempt("user", 1, null);
        when(repository.findByUsername("user")).thenReturn(Optional.of(attempt));
        assertThat(loginAttemptService.getRemainingBlockTime("user").isZero()).isTrue();
    }
}
