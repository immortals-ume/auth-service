package com.immortals.auth.service.audit;

import com.immortals.auth.service.audit.annotation.AuditAction;
import com.immortals.auth.service.domain.entity.AuditLog;
import com.immortals.auth.service.service.AuditLogService;
import com.immortals.logging.api.ILogger;
import com.immortals.logging.core.LogManager;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Aspect
@Component
public class AuditAspect {

    private static final ILogger log = LogManager.getInstance().getLogger(AuditAspect.class);

    private final AuditLogService auditLogService;

    public AuditAspect(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @Around("@annotation(auditAction)")
    public Object audit(ProceedingJoinPoint jp, AuditAction auditAction) throws Throwable {
        long start = System.currentTimeMillis();

        AuditRequestContext ctx = AuditRequestContext.current();
        String username = resolveUsername();

        try {
            Object result = jp.proceed();
            persist(auditAction, ctx, username, AuditStatus.SUCCESS, null, System.currentTimeMillis() - start);
            return result;
        } catch (Throwable t) {
            persist(auditAction, ctx, username, AuditStatus.ERROR, truncate(t.getMessage(), 2000), System.currentTimeMillis() - start);
            throw t;
        }
    }

    private void persist(AuditAction meta, AuditRequestContext ctx,
                         String username, AuditStatus status,
                         String errorMessage, long elapsedMs) {
        try {
            AuditLog entry = AuditLog.builder()
                    .auditDate(Instant.now())
                    .auditType(meta.type())
                    .action(meta.action())
                    .severity(meta.severity())
                    .username(username)
                    .ipAddress(ctx != null ? ctx.getIpAddress() : null)
                    .userAgent(ctx != null ? ctx.getUserAgent() : null)
                    .requestId(ctx != null ? ctx.getRequestId() : "N/A")
                    .sessionId(ctx != null ? ctx.getSessionId() : null)
                    .resourceType(meta.resourceType().isEmpty() ? null : meta.resourceType())
                    .status(status)
                    .errorMessage(errorMessage)
                    .executionTimeMs(elapsedMs)
                    .build();
            auditLogService.log(entry);
        } catch (Exception e) {
            log.error("AuditAspect failed to queue audit log for action='{}': {}", meta.action(), e.getMessage(), e);
        }
    }

    private String resolveUsername() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
                return auth.getName();
            }
        } catch (Exception ignored) {}
        return "anonymous";
    }

    private String truncate(String s, int max) {
        if (s == null) return null;
        return s.length() <= max ? s : s.substring(0, max);
    }
}

