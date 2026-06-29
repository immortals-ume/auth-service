package com.immortals.auth.service.service.exception;

public class UserException extends RuntimeException {
    public UserException(String message, Throwable e) {
        super(message, e);
    }

    public UserException(String message) {
        super(message);
    }
}

