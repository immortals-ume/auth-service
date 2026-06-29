package com.immortals.auth.service.audit.annotation;

import com.immortals.auth.service.audit.AuditSeverity;
import com.immortals.auth.service.audit.AuditType;

import java.lang.annotation.*;

/**
 * Marks a method for automatic audit logging via AuditAspect.
 * The aspect captures timing, principal, request context, and outcome
 * synchronously in the request thread, then persists asynchronously.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AuditAction {

    /** Business action label written to audit_log.action (e.g. "USER_LOGIN"). */
    String action();

    AuditType type() default AuditType.ACTION_AUDIT;

    AuditSeverity severity() default AuditSeverity.INFO;

    /** Logical resource category written to audit_log.resource_type (e.g. "USER"). */
    String resourceType() default "";
}

