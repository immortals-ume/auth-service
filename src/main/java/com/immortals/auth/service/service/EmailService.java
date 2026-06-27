package com.immortals.auth.service.service;

public interface EmailService {
    void sendEmailVerification(String to, String token);
    void sendPasswordReset(String to, String token);
}
