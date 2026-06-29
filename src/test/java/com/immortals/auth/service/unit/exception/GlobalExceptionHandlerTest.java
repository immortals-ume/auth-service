package com.immortals.auth.service.unit.exception;

import com.immortals.auth.service.controller.exception.GlobalExceptionHandler;
import com.immortals.auth.service.domain.ApiResponse;
import com.immortals.auth.service.service.exception.AuthenticationException;
import com.immortals.auth.service.service.exception.DuplicateResourceException;
import com.immortals.auth.service.service.exception.ResourceNotFoundException;
import com.immortals.auth.service.service.exception.UserException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import liquibase.exception.DatabaseException;
import org.hibernate.cache.CacheException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();
    private final MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/test");

    // ──────────────── ConstraintViolationException ────────────────

    @Test
    void handleConstraintViolation_returns400() {
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        when(violation.getPropertyPath()).thenReturn(mock(jakarta.validation.Path.class));
        when(violation.getInvalidValue()).thenReturn("bad");
        when(violation.getMessage()).thenReturn("must not be null");
        ConstraintViolationException ex = new ConstraintViolationException(Set.of(violation));

        ResponseEntity<ApiResponse<Void>> response = handler.handleConstraintViolation(ex, req);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(400);
    }

    // ──────────────── MissingServletRequestParameterException ────────────────

    @Test
    void handleMissingParam_returns400() {
        MissingServletRequestParameterException ex =
                new MissingServletRequestParameterException("token", "String");

        ResponseEntity<ApiResponse<Void>> response = handler.handleMissingParam(ex, req);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).contains("token");
    }

    // ──────────────── NoHandlerFoundException ────────────────

    @Test
    void handleNoHandler_returns404() {
        NoHandlerFoundException ex = new NoHandlerFoundException("GET", "/no/such", null);

        ResponseEntity<ApiResponse<Void>> response = handler.handleNoHandler(ex, req);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(404);
    }

    // ──────────────── NoResourceFoundException ────────────────

    @Test
    void handleNoResource_returns404() {
        NoResourceFoundException ex = new NoResourceFoundException(
                org.springframework.http.HttpMethod.GET, "/static/missing.js", null);

        ResponseEntity<ApiResponse<Void>> response = handler.handleNoResource(ex, req);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // ──────────────── HttpRequestMethodNotSupportedException ────────────────

    @Test
    void handleMethodNotSupported_returns405() {
        HttpRequestMethodNotSupportedException ex =
                new HttpRequestMethodNotSupportedException("DELETE");

        ResponseEntity<ApiResponse<Void>> response = handler.handleMethodNotSupported(ex, req);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
    }

    // ──────────────── AuthenticationException ────────────────

    @Test
    void handleAuthenticationException_returns401() {
        AuthenticationException ex = new AuthenticationException("invalid token");

        ResponseEntity<ApiResponse<Void>> response = handler.handleAuthenticationException(ex, req);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).contains("invalid token");
    }

    // ──────────────── UserException ────────────────

    @Test
    void handleUserException_returns400() {
        UserException ex = new UserException("user not found");

        ResponseEntity<ApiResponse<Void>> response = handler.handleUserException(ex, req);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).contains("user not found");
    }

    // ──────────────── DuplicateResourceException ────────────────

    @Test
    void handleDuplicateResource_returns409() {
        DuplicateResourceException ex = new DuplicateResourceException("email already taken");

        ResponseEntity<ApiResponse<Void>> response = handler.handleDuplicateResource(ex, req);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    // ──────────────── ResourceNotFoundException ────────────────

    @Test
    void handleResourceNotFound_returns404() {
        ResourceNotFoundException ex = new ResourceNotFoundException("user not found");

        ResponseEntity<ApiResponse<Void>> response = handler.handleResourceNotFound(ex, req);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // ──────────────── CacheException ────────────────

    @Test
    void handleCacheException_returns500() {
        CacheException ex = new CacheException("cache miss");

        ResponseEntity<ApiResponse<Void>> response = handler.handleCacheException(ex, req);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // ──────────────── DatabaseException ────────────────

    @Test
    void handleDatabaseException_returns500() throws Exception {
        DatabaseException ex = new DatabaseException("db down");

        ResponseEntity<ApiResponse<Void>> response = handler.handleDatabaseException(ex, req);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // ──────────────── HttpMessageNotReadableException ────────────────

    @Test
    void handleMessageNotReadable_returns400() {
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("bad body", (org.springframework.http.HttpInputMessage) null);

        ResponseEntity<ApiResponse<Void>> response = handler.handleMessageNotReadable(ex, req);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).contains("Malformed");
    }

    // ──────────────── AccessDeniedException ────────────────

    @Test
    void handleAccessDenied_returns403() {
        AccessDeniedException ex = new AccessDeniedException("forbidden");

        ResponseEntity<ApiResponse<Void>> response = handler.handleAccessDenied(ex, req);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    // ──────────────── DataIntegrityViolationException ────────────────

    @Test
    void handleDataIntegrityViolation_returns409() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException("unique constraint");

        ResponseEntity<ApiResponse<Void>> response = handler.handleDataIntegrityViolation(ex, req);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    // ──────────────── Generic Exception ────────────────

    @Test
    void handleGenericException_returns500() {
        RuntimeException ex = new RuntimeException("unexpected error");

        ResponseEntity<ApiResponse<Void>> response = handler.handleGenericException(ex, req);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // ──────────────── Correlation ID propagation ────────────────

    @Test
    void handleGenericException_withCorrelationIdHeader_includesItInResponse() {
        req.addHeader("X-Correlation-ID", "corr-123");
        RuntimeException ex = new RuntimeException("error");

        ResponseEntity<ApiResponse<Void>> response = handler.handleGenericException(ex, req);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
