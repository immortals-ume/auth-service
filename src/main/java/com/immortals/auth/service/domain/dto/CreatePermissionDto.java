package com.immortals.auth.service.domain.dto;

import jakarta.validation.constraints.NotBlank;

public record CreatePermissionDto(
        @NotBlank String name,
        String description
) {
}