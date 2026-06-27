package com.immortals.auth.service.domain.dto;

import jakarta.validation.constraints.NotNull;

public record AssignPermissionRequest(
        @NotNull Long roleId,
        @NotNull Long permissionId
) {
}