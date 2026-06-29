package com.immortals.auth.service.controller;

import com.immortals.auth.service.audit.AuditSeverity;
import com.immortals.auth.service.audit.AuditType;
import com.immortals.auth.service.audit.annotation.AuditAction;
import com.immortals.auth.service.domain.ApiResponse;
import com.immortals.auth.service.domain.dto.ForgotPasswordRequest;
import com.immortals.auth.service.domain.dto.LoginRequestDto;
import com.immortals.auth.service.domain.dto.LoginResponseDto;
import com.immortals.auth.service.domain.dto.ResetPasswordRequest;
import com.immortals.auth.service.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @AuditAction(action = "USER_LOGIN", type = AuditType.SECURITY_AUDIT, severity = AuditSeverity.HIGH, resourceType = "AUTH")
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(
            @RequestBody @Valid LoginRequestDto dto,
            HttpServletResponse response) {

        LoginResponseDto body = authService.login(dto, response);
        return ResponseEntity.ok(body);
    }

    @AuditAction(action = "USER_LOGOUT", type = AuditType.SECURITY_AUDIT, resourceType = "AUTH")
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        authService.logout(request, response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponseDto> refresh(
            HttpServletRequest request,
            HttpServletResponse response) {

        LoginResponseDto body = authService.refresh(request, response);
        return ResponseEntity.ok(body);
    }

    @AuditAction(action = "FORGOT_PASSWORD", type = AuditType.SECURITY_AUDIT, severity = AuditSeverity.WARNING, resourceType = "AUTH")
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @RequestBody @Valid ForgotPasswordRequest forgotPasswordRequest) {

        authService.forgotPassword(forgotPasswordRequest);
        return ResponseEntity.ok(ApiResponse.success(null,
                "If that email is registered, a reset link has been sent."));
    }

    @AuditAction(action = "PASSWORD_RESET", type = AuditType.SECURITY_AUDIT, severity = AuditSeverity.HIGH, resourceType = "AUTH")
    @PostMapping("/reset-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resetPassword(@RequestBody @Valid ResetPasswordRequest resetPasswordRequest) {
        authService.resetPassword(resetPasswordRequest);
    }
}

