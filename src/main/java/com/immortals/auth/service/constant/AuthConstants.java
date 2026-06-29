package com.immortals.auth.service.constant;

public final class AuthConstants {

    public static final String ANONYMOUS_USER = "anonymousUser";
    public static final String SYSTEM_PRINCIPAL = "SYSTEM";

    public static final String GENERIC_ERROR_MESSAGE =
            "Something went wrong. Please try again later.";

    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";

    public static final String MOBILE_REGEX_INDIA = "^[6-9]\\d{9}$";

    public static final String PASSWORD_REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[#$@!%&*?])[A-Za-z\\d#$@!%&*?]{8,16}$";

    public static final String EMAIL_REGEX = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$";

    public static final String TOKEN_PREFIX = "Bearer ";

    public static final String HEADER_STRING = "Authorization";

    public static final String MDC_USER_AGENT_KEY = "userAgent";

    public static final Long MAX_AGE_CORS_SECS = 86400L;

    public static final Long MAX_AGE_HSTS_SECS = 86400L;

    public static final String[] PUBLIC_RESOURCE_PATHS = {
            "/redirect",
            "/health",
            "/actuator/**",
            "/static/**",
            "/webjars/**",
            "/favicon.ico",
            "/css/**",
            "/js/**",
            "/images/**"
    };

    public static final String[] ANONYMOUS_API_PATHS = {
            "/api/v1/user/register",
            "/api/v1/user/verify-email/confirm",
            "/api/v1/auth/login",
            "/api/v1/auth/logout",
            "/api/v1/auth/refresh",
            "/api/v1/auth/forgot-password",
            "/api/v1/auth/reset-password"
    };
}


