package com.immortals.auth.service.security.provider;

import com.immortals.auth.service.audit.AuditingRevisionListener;
import com.immortals.auth.service.utils.ObjectMapperUtils;
import com.immortals.logging.api.ILogger;
import com.immortals.logging.core.LogManager;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


@Component
public class AuthEntryPoint implements AuthenticationEntryPoint {
    private static final ILogger log =
            LogManager.getInstance().getLogger(AuditingRevisionListener.class);

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException)
            throws ServletException {
        try {
            log.error("Unauthorized access attempt: {}", authException.getMessage());

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("status", HttpServletResponse.SC_UNAUTHORIZED);
            responseBody.put("error", "Unauthorized");
            responseBody.put("message", authException.getMessage());
            responseBody.put("path", request.getRequestURI());

            ObjectMapperUtils.mapper().writeValue(response.getOutputStream(), responseBody);
        } catch (IOException e) {
            log.error("Failed to write response: {}", e.getMessage());
            throw new ServletException("Unable to write response", e);
        }
    }

}
