package com.immortals.auth.service.service;

import com.immortals.auth.service.domain.dto.ForgotPasswordRequest;
import com.immortals.auth.service.domain.dto.LoginRequestDto;
import com.immortals.auth.service.domain.dto.LoginResponseDto;
import com.immortals.auth.service.domain.dto.ResetPasswordRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {

    LoginResponseDto login(LoginRequestDto loginDto, HttpServletResponse response);

    void logout(HttpServletRequest request, HttpServletResponse response);

    LoginResponseDto refresh(HttpServletRequest request, HttpServletResponse response);

    void forgotPassword(ForgotPasswordRequest forgotPasswordRequest);

    void resetPassword(ResetPasswordRequest resetPasswordRequest);
}

