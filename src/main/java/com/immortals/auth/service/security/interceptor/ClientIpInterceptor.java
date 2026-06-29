package com.immortals.auth.service.security.interceptor;

import com.immortals.auth.service.context.RequestContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import static com.immortals.auth.service.constant.AuthConstants.MDC_USER_AGENT_KEY;

@Component
public class ClientIpInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String userAgent = extractUserAgent(request);
        MDC.put(MDC_USER_AGENT_KEY, userAgent);
        request.setAttribute(MDC_USER_AGENT_KEY, userAgent);
        RequestContext.setUserAgent(userAgent);
        return Boolean.TRUE;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        MDC.remove(MDC_USER_AGENT_KEY);
        RequestContext.clear();
    }

    private String extractUserAgent(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        return (userAgent != null && !userAgent.isEmpty()) ? userAgent : "unknown";
    }
}

