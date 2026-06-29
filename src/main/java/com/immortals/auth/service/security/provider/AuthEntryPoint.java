package com.immortals.auth.service.security.provider;

import com.immortals.auth.service.domain.ApiResponse;
import com.immortals.auth.service.utils.ObjectMapperUtils;
import com.immortals.logging.api.ILogger;
import com.immortals.logging.core.LogManager;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static com.immortals.auth.service.constant.AuthConstants.CORRELATION_ID_HEADER;

@Component
public class AuthEntryPoint implements AuthenticationEntryPoint {

    private static final ILogger log = LogManager.getInstance().getLogger(AuthEntryPoint.class);

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException)
            throws ServletException {
        try {
            log.warn("Unauthorized access attempt on {}: {}", request.getRequestURI(), authException.getMessage());

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

            String correlationId = request.getHeader(CORRELATION_ID_HEADER);
            ApiResponse<Void> body = ApiResponse.error(
                    HttpStatus.UNAUTHORIZED.value(),
                    HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                    authException.getMessage(),
                    request.getRequestURI(),
                    correlationId
            );

            ObjectMapperUtils.mapper().writeValue(response.getOutputStream(), body);
        } catch (IOException e) {
            log.error("Failed to write 401 response: {}", e.getMessage());
            throw new ServletException("Unable to write response", e);
        }
    }
}

