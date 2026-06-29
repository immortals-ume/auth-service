package com.immortals.auth.service.audit;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

import static com.immortals.auth.service.constant.AuthConstants.CORRELATION_ID_HEADER;

/**
 * Populates AuditRequestContext for every incoming HTTP request.
 * Runs first in the filter chain so audit metadata is available
 * to the JWT filter, service layer, and Hibernate Envers revision listener.
 */
@Component
@Order(1)
public class AuditRequestContextFilter extends OncePerRequestFilter {

    private static final String[] IP_HEADERS = {
            "X-Forwarded-For", "X-Real-IP", "Proxy-Client-IP", "WL-Proxy-Client-IP"
    };

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String requestId = resolveRequestId(request);
        response.setHeader(CORRELATION_ID_HEADER, requestId);

        AuditRequestContext ctx = AuditRequestContext.create(
                requestId,
                resolveClientIp(request),
                request.getHeader("User-Agent"),
                request.getSession(false) != null ? request.getSession(false).getId() : null
        );
        AuditRequestContext.set(ctx);
        try {
            chain.doFilter(request, response);
        } finally {
            AuditRequestContext.clear();
        }
    }

    private String resolveRequestId(HttpServletRequest request) {
        String correlationId = request.getHeader(CORRELATION_ID_HEADER);
        return (correlationId != null && !correlationId.isBlank())
                ? correlationId
                : UUID.randomUUID().toString();
    }

    private String resolveClientIp(HttpServletRequest request) {
        for (String header : IP_HEADERS) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isBlank() && !"unknown".equalsIgnoreCase(ip)) {
                return ip.split(",")[0].trim();
            }
        }
        return request.getRemoteAddr();
    }
}

