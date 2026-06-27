package com.immortals.auth.service.service;

import com.immortals.auth.service.domain.dto.RegisterRequestDTO;
import com.immortals.auth.service.domain.dto.RegisterResponseDTO;

public interface UserService {

    RegisterResponseDTO register(RegisterRequestDTO registerDto);

    void sendEmailVerification();

    void verifyEmail(String token);

    void sendPhoneOtp();

    void verifyPhoneOtp(String otp);
}
