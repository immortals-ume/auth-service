package com.immortals.auth.service.exception;

public class AuthenticationException extends RuntimeException {
    public AuthenticationException(String msg, Throwable e) {
        super(msg,e);
    }

    public AuthenticationException(String msg) {
        super(msg);
    }
}
