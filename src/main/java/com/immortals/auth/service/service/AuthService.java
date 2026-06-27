package com.immortals.auth.service.service;

import com.immortals.auth.service.domain.dto.ForgotPasswordRequest;
import com.immortals.auth.service.domain.dto.LoginRequestDto;
import com.immortals.auth.service.domain.dto.LoginResponseDto;
import com.immortals.auth.service.domain.dto.ResetPasswordRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

public interface AuthService {

    LoginResponseDto login(@Valid LoginRequestDto loginDto, HttpServletResponse response);

    void logout(HttpServletRequest request, HttpServletResponse response);

    LoginResponseDto refresh(HttpServletRequest request, HttpServletResponse response);

    void forgotPassword(@Valid ForgotPasswordRequest forgotPasswordRequest);

    void resetPassword(@Valid ResetPasswordRequest resetPasswordRequest);
}
