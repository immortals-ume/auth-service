package com.immortals.auth.service.domain.entity;

import com.immortals.auth.service.audit.AuditSeverity;
import com.immortals.auth.service.audit.AuditStatus;
import com.immortals.auth.service.audit.AuditType;
import jakarta.persistence.*;

import java.time.Instant;

/**
 * Append-only audit log entry. Not itself @Audited to avoid infinite recursion.
 * Written asynchronously via AuditLogService; never updated after insert.
 */
@Entity
@Table(name = "audit_log", schema = "public")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "audit_log_seq")
    @SequenceGenerator(name = "audit_log_seq", sequenceName = "audit_log_seq",
            schema = "public", allocationSize = 1)
    private Long id;

    @Column(name = "audit_date", nullable = false)
    private Instant auditDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "audit_type", nullable = false, length = 50)
    private AuditType auditType;

    @Column(name = "action", nullable = false, length = 100)
    private String action;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", length = 20)
    private AuditSeverity severity;

    @Column(name = "user_id", length = 100)
    private String userId;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "session_id")
    private String sessionId;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "request_id", nullable = false)
    private String requestId;

    @Column(name = "resource_type", length = 100)
    private String resourceType;

    @Column(name = "resource_id")
    private String resourceId;

    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue;

    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;

    @Column(name = "change_description", columnDefinition = "TEXT")
    private String changeDescription;

    @Column(name = "additional_context", columnDefinition = "JSONB")
    private String additionalContext;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AuditStatus status;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "execution_time_ms", nullable = false)
    private long executionTimeMs;

    @Column(name = "created_date", nullable = false, updatable = false)
    private Instant createdDate;

    protected AuditLog() {}

    private AuditLog(Builder b) {
        this.auditDate = b.auditDate;
        this.auditType = b.auditType;
        this.action = b.action;
        this.severity = b.severity;
        this.userId = b.userId;
        this.username = b.username;
        this.sessionId = b.sessionId;
        this.ipAddress = b.ipAddress;
        this.userAgent = b.userAgent;
        this.requestId = b.requestId;
        this.resourceType = b.resourceType;
        this.resourceId = b.resourceId;
        this.oldValue = b.oldValue;
        this.newValue = b.newValue;
        this.changeDescription = b.changeDescription;
        this.additionalContext = b.additionalContext;
        this.status = b.status;
        this.errorMessage = b.errorMessage;
        this.executionTimeMs = b.executionTimeMs;
        this.createdDate = Instant.now();
    }

    public static Builder builder() { return new Builder(); }

    public Long getId() { return id; }
    public Instant getAuditDate() { return auditDate; }
    public AuditType getAuditType() { return auditType; }
    public String getAction() { return action; }
    public AuditSeverity getSeverity() { return severity; }
    public String getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getSessionId() { return sessionId; }
    public String getIpAddress() { return ipAddress; }
    public String getUserAgent() { return userAgent; }
    public String getRequestId() { return requestId; }
    public String getResourceType() { return resourceType; }
    public String getResourceId() { return resourceId; }
    public String getOldValue() { return oldValue; }
    public String getNewValue() { return newValue; }
    public String getChangeDescription() { return changeDescription; }
    public String getAdditionalContext() { return additionalContext; }
    public AuditStatus getStatus() { return status; }
    public String getErrorMessage() { return errorMessage; }
    public long getExecutionTimeMs() { return executionTimeMs; }
    public Instant getCreatedDate() { return createdDate; }

    public static final class Builder {
        private Instant auditDate = Instant.now();
        private AuditType auditType = AuditType.ACTION_AUDIT;
        private String action;
        private AuditSeverity severity = AuditSeverity.INFO;
        private String userId;
        private String username = "anonymous";
        private String sessionId;
        private String ipAddress;
        private String userAgent;
        private String requestId;
        private String resourceType;
        private String resourceId;
        private String oldValue;
        private String newValue;
        private String changeDescription;
        private String additionalContext;
        private AuditStatus status = AuditStatus.SUCCESS;
        private String errorMessage;
        private long executionTimeMs;

        private Builder() {}

        public Builder auditDate(Instant v)          { auditDate = v; return this; }
        public Builder auditType(AuditType v)         { auditType = v; return this; }
        public Builder action(String v)               { action = v; return this; }
        public Builder severity(AuditSeverity v)      { severity = v; return this; }
        public Builder userId(String v)               { userId = v; return this; }
        public Builder username(String v)             { username = v; return this; }
        public Builder sessionId(String v)            { sessionId = v; return this; }
        public Builder ipAddress(String v)            { ipAddress = v; return this; }
        public Builder userAgent(String v)            { userAgent = v; return this; }
        public Builder requestId(String v)            { requestId = v; return this; }
        public Builder resourceType(String v)         { resourceType = v; return this; }
        public Builder resourceId(String v)           { resourceId = v; return this; }
        public Builder oldValue(String v)             { oldValue = v; return this; }
        public Builder newValue(String v)             { newValue = v; return this; }
        public Builder changeDescription(String v)    { changeDescription = v; return this; }
        public Builder additionalContext(String v)    { additionalContext = v; return this; }
        public Builder status(AuditStatus v)          { status = v; return this; }
        public Builder errorMessage(String v)         { errorMessage = v; return this; }
        public Builder executionTimeMs(long v)        { executionTimeMs = v; return this; }

        public AuditLog build() {
            if (action == null || action.isBlank()) throw new IllegalStateException("action is required");
            if (requestId == null || requestId.isBlank()) requestId = "N/A";
            return new AuditLog(this);
        }
    }
}

