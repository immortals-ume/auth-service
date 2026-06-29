package com.immortals.auth.service.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateRoleRequest(
        @NotBlank @Size(max = 20) String roleName,
        @Size(max = 200) String description
) {
}

