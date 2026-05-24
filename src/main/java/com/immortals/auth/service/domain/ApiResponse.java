package com.immortals.auth.service.domain;

import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.List;

public record ApiResponse<T>(
        int status,
        String statusText,
        boolean success,
        T data,
        String message,
        String errorCode,
        String path,
        Instant timestamp,
        String correlationId,
        List<ValidationError> validationErrors
) {

    private static <T> ApiResponse<T> of(
            HttpStatus status,
            boolean success,
            T data,
            String message,
            String errorCode,
            String path,
            String correlationId,
            List<ValidationError> validationErrors
    ) {
        return new ApiResponse<>(
                status.value(),
                status.getReasonPhrase(),
                success,
                data,
                message,
                errorCode,
                path,
                Instant.now(),
                correlationId,
                validationErrors
        );
    }

    public static <T> ApiResponse<T> success(T data) {
        return success(data, null, null);
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return success(data, message, null);
    }

    public static <T> ApiResponse<T> success(T data, String message, String correlationId) {
        return of(
                HttpStatus.OK,
                true,
                data,
                message,
                ErrorCode.SUCCESS_OK,
                null,
                correlationId,
                null
        );
    }

    public static <T> ApiResponse<T> created(T data, String message) {
        return of(
                HttpStatus.CREATED,
                true,
                data,
                message,
                ErrorCode.SUCCESS_CREATED,
                null,
                null,
                null
        );
    }

    public static <T> ApiResponse<T> accepted(String message) {
        return of(
                HttpStatus.ACCEPTED,
                true,
                null,
                message,
                ErrorCode.SUCCESS_ACCEPTED,
                null,
                null,
                null
        );
    }

    public static <T> ApiResponse<T> noContent(String message) {
        return of(
                HttpStatus.NO_CONTENT,
                true,
                null,
                message,
                ErrorCode.SUCCESS_NO_CONTENT,
                null,
                null,
                null
        );
    }


    public static <T> ApiResponse<T> informational(String message, String code) {
        HttpStatus status = ErrorCode.getHttpStatus(code);
        return of(status, true, null, message, code, null, null, null);
    }

    public static <T> ApiResponse<T> processing(String message) {
        return informational(message, ErrorCode.INFO_PROCESSING);
    }


    public static <T> ApiResponse<T> error(
            String message,
            String errorCode,
            String path,
            String correlationId
    ) {
        HttpStatus status = ErrorCode.getHttpStatus(errorCode);

        return of(
                status,
                false,
                null,
                message,
                errorCode,
                path,
                correlationId,
                null
        );
    }

    public static <T> ApiResponse<T> error(
            int status,
            String statusText,
            String message,
            String path,
            String correlationId
    ) {
        return new ApiResponse<>(
                status,
                statusText,
                false,
                null,
                message,
                null,
                path,
                Instant.now(),
                correlationId,
                null
        );
    }

    public static <T> ApiResponse<T> validationError(
            int status,
            String statusText,
            String message,
            String path,
            String correlationId,
            List<ValidationError> validationErrors
    ) {
        return new ApiResponse<>(
                status,
                statusText,
                false,
                null,
                message,
                null,
                path,
                Instant.now(),
                correlationId,
                validationErrors
        );
    }


    public record ValidationError(
            String field,
            Object rejectedValue,
            String message,
            String code
    ) {
        public static ValidationError of(String field, Object rejectedValue, String message) {
            return new ValidationError(field, rejectedValue, message, null);
        }

        public static ValidationError of(String field, Object rejectedValue, String message, String code) {
            return new ValidationError(field, rejectedValue, message, code);
        }
    }
}