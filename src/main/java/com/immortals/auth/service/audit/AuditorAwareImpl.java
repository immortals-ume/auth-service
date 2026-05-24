package com.immortals.auth.service.audit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@Slf4j
public class AuditorAwareImpl implements AuditorAware<String> {

    private static final String ANONYMOUS_USER = "anonymousUser";

    private final String systemUser;

    public AuditorAwareImpl(@Value("${app.audit.system-user}") String systemUser) {
        this.systemUser = systemUser;
    }

    @Override
    public Optional<String> getCurrentAuditor() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();

            if (auth == null || !auth.isAuthenticated()) {
                return Optional.of(systemUser);
            }

            String username = auth.getName();

            if (username == null || username.isBlank() || ANONYMOUS_USER.equals(username)) {
                return Optional.of(systemUser);
            }

            return Optional.of(username);

        } catch (Exception e) {
            log.warn("Failed to resolve auditor, falling back to system user", e);
            return Optional.of(systemUser);
        }
    }
}