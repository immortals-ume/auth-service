package com.immortals.auth.service.domain.dto;

import com.immortals.auth.service.domain.enums.AuthProvider;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequestDto(
        String userName,
        @NotBlank @Email String email,
        String mobileNumber,
        @NotBlank String password,
        Boolean rememberMe,
        AuthProvider authProvider
) { }

