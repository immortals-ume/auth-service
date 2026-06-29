package com.immortals.auth.service.audit;

/**
 * Thread-local holder for per-request audit metadata.
 * Populated by AuditRequestContextFilter at the start of every HTTP request
 * and consumed by AuditAspect and AuditingRevisionListener.
 */
public final class AuditRequestContext {

    private static final ThreadLocal<AuditRequestContext> HOLDER = new ThreadLocal<>();

    private String requestId;
    private String ipAddress;
    private String userAgent;
    private String sessionId;
    private long startTimeMs;

    private AuditRequestContext() {}

    public static AuditRequestContext current() {
        return HOLDER.get();
    }

    static void set(AuditRequestContext ctx) {
        HOLDER.set(ctx);
    }

    static void clear() {
        HOLDER.remove();
    }

    static AuditRequestContext create(String requestId, String ipAddress, String userAgent, String sessionId) {
        AuditRequestContext ctx = new AuditRequestContext();
        ctx.requestId = requestId;
        ctx.ipAddress = ipAddress;
        ctx.userAgent = userAgent;
        ctx.sessionId = sessionId;
        ctx.startTimeMs = System.currentTimeMillis();
        return ctx;
    }

    public String getRequestId() { return requestId; }
    public String getIpAddress() { return ipAddress; }
    public String getUserAgent() { return userAgent; }
    public String getSessionId() { return sessionId; }
    public long getStartTimeMs() { return startTimeMs; }
}

