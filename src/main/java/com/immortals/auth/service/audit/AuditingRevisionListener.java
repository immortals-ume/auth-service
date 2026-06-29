package com.immortals.auth.service.audit;

import com.immortals.auth.service.context.ApplicationContextHolder;
import com.immortals.auth.service.domain.AuditingRevisionEntity;
import com.immortals.logging.api.ILogger;
import com.immortals.logging.core.LogManager;
import org.hibernate.envers.RevisionListener;

import static com.immortals.auth.service.constant.AuthConstants.SYSTEM_PRINCIPAL;

public class AuditingRevisionListener implements RevisionListener {

    private static final ILogger log =
            LogManager.getInstance().getLogger(AuditingRevisionListener.class);

    @Override
    public void newRevision(Object revisionEntity) {
        AuditingRevisionEntity entity = (AuditingRevisionEntity) revisionEntity;
        entity.setUsername(resolveUser());

        AuditRequestContext ctx = AuditRequestContext.current();
        if (ctx != null) {
            entity.setIpAddress(ctx.getIpAddress());
            entity.setUserAgent(ctx.getUserAgent());
        }
    }

    private String resolveUser() {
        if (!ApplicationContextHolder.isReady()) {
            log.warn("ApplicationContext not ready — falling back to '{}'", SYSTEM_PRINCIPAL);
            return SYSTEM_PRINCIPAL;
        }

        try {
            UserAuditResolver resolver = ApplicationContextHolder.getBean(UserAuditResolver.class);
            return resolver.resolve();
        } catch (Exception e) {
            log.warn("UserAuditResolver unavailable — falling back to '{}'", SYSTEM_PRINCIPAL, e);
            return SYSTEM_PRINCIPAL;
        }
    }
}
