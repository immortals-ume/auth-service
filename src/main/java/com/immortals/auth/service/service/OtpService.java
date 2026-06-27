package com.immortals.auth.service.service;

public interface OtpService {
    void generateAndSend(String phoneNumber);
    boolean verify(String phoneNumber, String otp);
    void invalidate(String phoneNumber);
}
