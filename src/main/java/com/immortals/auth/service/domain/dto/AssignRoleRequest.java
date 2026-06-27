package com.immortals.auth.service.domain.dto;

import jakarta.validation.constraints.NotNull;

public record AssignRoleRequest(
        @NotNull Long userId,
        @NotNull Long roleId
) {
}