package com.immortals.auth.service.audit;

import com.immortals.auth.service.domain.AuditingRevisionEntity;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.envers.RevisionListener;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Slf4j
public class AuditingRevisionListener implements RevisionListener {

    private static final String DEFAULT_SYSTEM_USER = "SYSTEM";
    private static final String ANONYMOUS_USER = "anonymousUser";

    @Override
    public void newRevision(Object revisionEntity) {

        AuditingRevisionEntity entity = (AuditingRevisionEntity) revisionEntity;

        String currentUser = resolveCurrentUser();

        entity.setUsername(currentUser);
    }

    private String resolveCurrentUser() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();

            if (auth == null || !auth.isAuthenticated()) {
                return DEFAULT_SYSTEM_USER;
            }

            String username = auth.getName();

            if (username == null || ANONYMOUS_USER.equals(username)) {
                return DEFAULT_SYSTEM_USER;
            }

            return username;

        } catch (Exception e) {
            log.warn("Failed to resolve current user for auditing. Falling back to SYSTEM.", e);
            return DEFAULT_SYSTEM_USER;
        }
    }
}