package com.immortals.auth.service.domain.dto;


public record LoginRequestDto(
        String userName,
        String email,
        String mobileNumber,
        String password,
        Boolean rememberMe,
        String authProvider
) {
}

