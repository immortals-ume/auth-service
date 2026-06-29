package com.immortals.auth.service.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetCredentials(
        @NotBlank String userName,
        @NotBlank @Size(min = 8, max = 64) String oldPassword,
        @NotBlank @Size(min = 8, max = 64) String password,
        @NotBlank @Size(min = 8, max = 64) String reTypePassword
) {
}

