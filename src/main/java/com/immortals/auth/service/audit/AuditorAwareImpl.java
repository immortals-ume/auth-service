package com.immortals.auth.service.audit;

import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("auditorProvider")
public class AuditorAwareImpl implements AuditorAware<String> {

    private final UserAuditResolver resolver;

    public AuditorAwareImpl(UserAuditResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public Optional<String> getCurrentAuditor() {
        return Optional.of(resolver.resolve());
    }
}