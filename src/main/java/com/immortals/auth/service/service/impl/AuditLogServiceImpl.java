package com.immortals.auth.service.service.impl;

import com.immortals.auth.service.db.context.DbContextHolder;
import com.immortals.auth.service.db.props.DbType;
import com.immortals.auth.service.domain.entity.AuditLog;
import com.immortals.auth.service.repository.AuditLogRepository;
import com.immortals.auth.service.service.AuditLogService;
import com.immortals.logging.api.ILogger;
import com.immortals.logging.core.LogManager;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditLogServiceImpl implements AuditLogService {

    private static final ILogger log = LogManager.getInstance().getLogger(AuditLogServiceImpl.class);

    private final AuditLogRepository auditLogRepository;

    public AuditLogServiceImpl(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * Runs in the @Async executor thread pool. The AuditLog is already fully
     * populated by the caller in the request thread, so thread-local context
     * (AuditRequestContext, SecurityContext) is not needed here.
     *
     * DbContextHolder is set manually because Spring AOP (@WriteOnly) cannot
     * intercept calls invoked from within the async executor — the proxy is
     * not on the call stack in the new thread.
     */
    @Async
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(AuditLog entry) {
        DbContextHolder.set(DbType.WRITE);
        try {
            auditLogRepository.save(entry);
        } catch (Exception e) {
            log.error("Failed to persist audit log entry for action='{}': {}", entry.getAction(), e.getMessage(), e);
        } finally {
            DbContextHolder.clear();
        }
    }
}

