package com.immortals.auth.service.controller.exception;

import com.immortals.auth.service.domain.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import liquibase.exception.DatabaseException;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.cache.CacheException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String GENERIC_ERROR_MESSAGE =
            "Something went wrong. Please try again later.";

    private static final String CORRELATION_ID_HEADER = "X-Correlation-Id";


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        List<ApiResponse.ValidationError> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::mapFieldError)
                .toList();

        return badRequest(
                ApiResponse.validationError(
                        HttpStatus.BAD_REQUEST.value(),
                        HttpStatus.BAD_REQUEST.getReasonPhrase(),
                        "Validation failed",
                        request.getRequestURI(),
                        getCorrelationId(request),
                        errors
                )
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(
            ConstraintViolationException ex,
            HttpServletRequest request) {

        List<ApiResponse.ValidationError> errors = ex.getConstraintViolations()
                .stream()
                .map(this::mapConstraintViolation)
                .toList();

        return badRequest(
                ApiResponse.validationError(
                        HttpStatus.BAD_REQUEST.value(),
                        HttpStatus.BAD_REQUEST.getReasonPhrase(),
                        "Constraint violation",
                        request.getRequestURI(),
                        getCorrelationId(request),
                        errors
                )
        );
    }

    @ExceptionHandler(TypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(
            TypeMismatchException ex,
            HttpServletRequest request) {

        String message = String.format(
                "Invalid value '%s' for '%s'. Expected type: %s",
                ex.getValue(),
                ex.getPropertyName(),
                ex.getRequiredType() != null
                        ? ex.getRequiredType().getSimpleName()
                        : "unknown"
        );

        return badRequest(
                ApiResponse.error(
                        HttpStatus.BAD_REQUEST.value(),
                        HttpStatus.BAD_REQUEST.getReasonPhrase(),
                        message,
                        request.getRequestURI(),
                        getCorrelationId(request)
                )
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request) {

        String message = String.format(
                "Parameter '%s' should be of type %s",
                ex.getName(),
                ex.getRequiredType() != null
                        ? ex.getRequiredType().getSimpleName()
                        : "unknown"
        );

        return badRequest(
                ApiResponse.error(
                        HttpStatus.BAD_REQUEST.value(),
                        HttpStatus.BAD_REQUEST.getReasonPhrase(),
                        message,
                        request.getRequestURI(),
                        getCorrelationId(request)
                )
        );
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingParam(
            MissingServletRequestParameterException ex,
            HttpServletRequest request) {

        return badRequest(
                ApiResponse.error(
                        HttpStatus.BAD_REQUEST.value(),
                        HttpStatus.BAD_REQUEST.getReasonPhrase(),
                        "Missing parameter: " + ex.getParameterName(),
                        request.getRequestURI(),
                        getCorrelationId(request)
                )
        );
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoHandler(
            NoHandlerFoundException ex,
            HttpServletRequest request) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ApiResponse.error(
                        HttpStatus.NOT_FOUND.value(),
                        HttpStatus.NOT_FOUND.getReasonPhrase(),
                        "No handler found for " + ex.getRequestURL(),
                        request.getRequestURI(),
                        getCorrelationId(request)
                )
        );
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex,
            HttpServletRequest request) {

        String supported = ex.getSupportedHttpMethods() != null
                ? ex.getSupportedHttpMethods().toString()
                : "";

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(
                ApiResponse.error(
                        HttpStatus.METHOD_NOT_ALLOWED.value(),
                        HttpStatus.METHOD_NOT_ALLOWED.getReasonPhrase(),
                        ex.getMethod() + " not supported. Supported: " + supported,
                        request.getRequestURI(),
                        getCorrelationId(request)
                )
        );
    }


    @ExceptionHandler(CacheException.class)
    public ResponseEntity<ApiResponse<Void>> handleCacheException(
            CacheException ex,
            HttpServletRequest request) {

        log.error("Cache exception", ex);

        return internalError(
                ApiResponse.error(
                        "Cache error occurred",
                        "CACHE_ERROR",
                        request.getRequestURI(),
                        getCorrelationId(request)
                )
        );
    }

    @ExceptionHandler(DatabaseException.class)
    public ResponseEntity<ApiResponse<Void>> handleDatabaseException(
            DatabaseException ex,
            HttpServletRequest request) {

        log.error("Database exception", ex);

        return internalError(
                ApiResponse.error(
                        "Database error occurred",
                        "DATABASE_ERROR",
                        request.getRequestURI(),
                        getCorrelationId(request)
                )
        );
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(
            Exception ex,
            HttpServletRequest request) {

        log.error("Unhandled exception", ex);

        return internalError(
                ApiResponse.error(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                        GENERIC_ERROR_MESSAGE,
                        request.getRequestURI(),
                        getCorrelationId(request)
                )
        );
    }

    private ResponseEntity<ApiResponse<Void>> badRequest(ApiResponse<Void> body) {
        return ResponseEntity.badRequest().body(body);
    }

    private ResponseEntity<ApiResponse<Void>> internalError(ApiResponse<Void> body) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    private ApiResponse.ValidationError mapFieldError(FieldError error) {
        return ApiResponse.ValidationError.of(
                error.getField(),
                error.getRejectedValue(),
                error.getDefaultMessage(),
                error.getCode()
        );
    }

    private ApiResponse.ValidationError mapConstraintViolation(ConstraintViolation<?> violation) {
        return ApiResponse.ValidationError.of(
                violation.getPropertyPath().toString(),
                violation.getInvalidValue(),
                violation.getMessage(),
                null
        );
    }

    private String getCorrelationId(HttpServletRequest request) {
        Object id = request.getHeader(CORRELATION_ID_HEADER);
        if (id == null) {
            id = request.getAttribute(CORRELATION_ID_HEADER);
        }
        return id != null ? id.toString() : null;
    }
}