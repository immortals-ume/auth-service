package com.immortals.auth.service.service;


import com.immortals.auth.service.domain.dto.LoginRequestDto;
import com.immortals.auth.service.domain.dto.LoginResponseDto;
import jakarta.validation.Valid;

public interface AuthService {
    LoginResponseDto login(@Valid LoginRequestDto loginDto);
}
