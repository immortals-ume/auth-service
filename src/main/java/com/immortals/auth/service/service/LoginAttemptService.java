package com.immortals.auth.service.service;

import java.time.Duration;

public interface LoginAttemptService {

    void loginSucceeded(String username);

    void loginFailed(String username);

    boolean isBlocked(String username);

    void resetAttempts(String username);

    int getFailedAttempts(String username);

    Duration getRemainingBlockTime(String username);
}
