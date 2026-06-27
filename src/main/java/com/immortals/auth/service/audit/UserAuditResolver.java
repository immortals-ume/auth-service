package com.immortals.auth.service.audit;

import com.immortals.auth.service.domain.entity.SystemAuditUser;
import com.immortals.auth.service.repository.SystemAuditUserRepository;
import com.immortals.logging.api.ILogger;
import com.immortals.logging.core.LogManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.immortals.auth.service.constant.AuthConstants.ANONYMOUS_USER;
import static com.immortals.auth.service.constant.AuthConstants.SYSTEM_PRINCIPAL;

@Service
public class UserAuditResolver {
    private static final ILogger log = LogManager.getInstance().getLogger(UserAuditResolver.class);

    private final SystemAuditUserRepository systemAuditUserRepository;

    public UserAuditResolver(SystemAuditUserRepository systemAuditUserRepository) {
        this.systemAuditUserRepository = systemAuditUserRepository;
    }

    public String resolve() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();

            if (auth != null && auth.isAuthenticated()) {
                String name = auth.getName();

                if (name != null && !name.isBlank() && !ANONYMOUS_USER.equals(name)) {
                    return name;
                }
            }
        } catch (Exception e) {
            log.warn("SecurityContext unavailable during audit resolution", e);
        }

        try {
            Optional<SystemAuditUser> systemAuditUser =
                    systemAuditUserRepository.findActiveSystemUser();

            if (systemAuditUser.isPresent()) {
                return systemAuditUser.get().getUsername();
            }

        } catch (Exception e) {
            log.error("SYSTEM_AUDIT table lookup failed during audit resolution", e);
        }

        return SYSTEM_PRINCIPAL;
    }
}