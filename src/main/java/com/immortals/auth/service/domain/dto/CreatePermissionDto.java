package com.immortals.auth.service.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreatePermissionDto(
        @NotBlank @Size(max = 50) String name,
        @Size(max = 200) String description
) {
}
