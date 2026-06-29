package com.immortals.auth.service.service;

import com.immortals.auth.service.domain.entity.AuditLog;

public interface AuditLogService {

    /**
     * Persists an audit log entry asynchronously on the write datasource.
     * Callers must fully populate the entry before invoking — the context
     * (IP, userAgent, requestId) from the request thread must be captured
     * before the async handoff.
     */
    void log(AuditLog entry);
}

