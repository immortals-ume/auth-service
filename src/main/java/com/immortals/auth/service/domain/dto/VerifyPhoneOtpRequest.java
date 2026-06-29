package com.immortals.auth.service.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VerifyPhoneOtpRequest(@NotBlank @Size(min = 6, max = 6) String otp) {}

